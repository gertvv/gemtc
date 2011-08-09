/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.drugis.mtc

import org.apache.commons.math.stat.descriptive.rank.Percentile
import collection.JavaConversions._

trait NetworkModelParameter extends Parameter {
	def getName: String = toString
}

class BasicParameter(val base: Treatment, val subject: Treatment)
extends NetworkModelParameter {
	override def toString = "d." + base.id + "." + subject.id

	override def equals(other: Any): Boolean = other match {
		case p: BasicParameter => (p.base == base && p.subject == subject)
		case _ => false
	}

	override def hashCode: Int = 31 * base.hashCode + subject.hashCode
}

class SplitParameter(
	val base: Treatment,
	val subject: Treatment,
	val direct: Boolean)
extends NetworkModelParameter {
	override def toString =  "d." + base.id + "." + subject.id +
		{ if (direct) ".dir" else ".ind" }

	override def equals(other: Any): Boolean = other match {
		case p: SplitParameter =>
			(p.base == base && p.subject == subject && p.direct == direct)
		case _ => false
	}

	override def hashCode: Int =
		31 * (31 * base.hashCode + subject.hashCode) + direct.hashCode
}

final class InconsistencyParameter(val cycle: List[Treatment])
extends NetworkModelParameter {
	def this(list: java.util.List[Treatment]) {
		this(asBuffer(list).toList)
	}

	private val cycleStr =
		cycle.reverse.tail.reverse.map(t => t.id).mkString(".")

	override def toString() = "w." + cycleStr

	override def equals(other: Any): Boolean = other match {
		case p: InconsistencyParameter => (p.cycle == cycle)
		case _ => false
	}

	override def hashCode: Int = cycle.hashCode

	def treatmentList: java.util.List[Treatment] = {
		val list = new java.util.ArrayList[Treatment]()
		for (t <- cycle) {
			list.add(t)
		}
		list
	}
}

/**
 * Class representing a Bayes model for a treatment network
 */
class NetworkModel[M <: Measurement, P <: Parametrization[M]](
	val parametrization: P,
	val studyBaseline: Map[Study[M], Treatment],
	val treatmentList: List[Treatment],
	val studyList: List[Study[M]]) {

	val network: Network[M] = parametrization.network
	val basis: FundamentalGraphBasis[Treatment] = parametrization.basis

	require(Set[Study[M]]() ++ studyList == network.studies)
	require(Set[Treatment]() ++ treatmentList == network.treatments)
	require(studyBaseline.keySet == network.studies)
	require(basis.tree.vertexSet == network.treatments)

	val studyMap = NetworkModel.indexMap(studyList)
	val treatmentMap = NetworkModel.indexMap(treatmentList)

	val data = studyList.flatMap(
		study => NetworkModel.treatmentList(study.treatments).map(
				t => (study, study.measurements(t))))

	/**
	 * Basic parameters
	 */
	val basicParameters = parametrization.basicParameters

	/**
	 * Inconsistency parameters
	 */
	val inconsistencyParameters:List[InconsistencyParameter] =
	parametrization match {
		case ip: InconsistencyParametrization[M] => ip.inconsistencyParameters
		case _ => List[InconsistencyParameter]()
	}

	/**
	 * Full list of parameters
	 */
	val parameterVector: List[NetworkModelParameter] =
		parametrization.parameterVector

	/**
	 * List of relative effects in order
	 */
	val relativeEffects: List[(Treatment, Treatment)] =
		studyList.flatMap(study => studyRelativeEffects(study))

	val relativeEffectIndex: Map[Study[M], Int] =
		reIndexMap(studyList, 0)

	private def reIndexMap(l: List[Study[M]], i: Int)
	: Map[Study[M], Int] = l match {
		case Nil => Map[Study[M], Int]()
		case s :: l0 =>
			reIndexMap(l0, i + s.treatments.size - 1) + ((s, i))
	}

	def studyRelativeEffects(study: Study[M])
	: List[(Treatment, Treatment)] =
		for {t <- treatmentList; if (study.treatments.contains(t)
				&& !(studyBaseline(study) == t))
			} yield (studyBaseline(study), t)

	
	/**
	 * Swap the members of a pair
	 */
	def swap[T](e: (T, T)) = (e._2, e._1)

	/**
	 * Get the node in study that is to be split (or None).
	 */
	def splitNode(study: Study[M])
	: Option[(Treatment, Treatment)] = parametrization match {
		case splt: NodeSplitParametrization[M] => {
			val re = studyRelativeEffects(study)
			val splitNode = splt.splitNode
			if (re.contains(splitNode)) Some(splitNode)
			else if (re.contains(swap(splitNode))) Some(swap(splitNode))
			else None
		}
		case _ => None
	}


	def variancePrior: Double = {
		val cls = network.measurementType
		val means =
			if (cls == classOf[DichotomousMeasurement])
				dichMeans()
			else if (cls == classOf[ContinuousMeasurement])
				contMeans()
			else
				throw new IllegalStateException("Unknown measurement type " +
						cls)
		range(means)
	}

	def normalPrior: Double = {
		val x = variancePrior * 15
		return x * x;
	}

	private def range(x: List[Double]): Double = {
		val min = x.reduceLeft(Math.min)
		val max = x.reduceLeft(Math.max)
		max - min
	}

	private def dichMeans(): List[Double] = {
		for {
			s <- studyList;
			t1 <- s.treatments;
			t2 <- (s.treatments - t1)
		} yield
			Math.abs(logOdds(s.measurements(t1).asInstanceOf[DichotomousMeasurement]) - logOdds(s.measurements(t2).asInstanceOf[DichotomousMeasurement]))
	}

	private def logOdds(m: DichotomousMeasurement): Double = {
		Math.log((m.responders + 0.5) / (m.sampleSize - m.responders + 0.5))
	}

	private def contMeans(): List[Double] = {
		for {
			s <- studyList;
			t1 <- s.treatments;
			t2 <- (s.treatments - t1)
		} yield
			Math.abs(s.measurements(t1).asInstanceOf[ContinuousMeasurement].mean - s.measurements(t2).asInstanceOf[ContinuousMeasurement].mean)
	}
}

object ConsistencyNetworkModel extends NetworkModelUtil {
	def apply[M <: Measurement](network: Network[M], tree: Tree[Treatment],
		baselines: Map[Study[M], Treatment])
	: NetworkModel[M, ConsistencyParametrization[M]] = {
		val pmtz = new ConsistencyParametrization(network,
			new FundamentalGraphBasis(network.treatmentGraph, tree))
		new NetworkModel[M, ConsistencyParametrization[M]](
			pmtz, baselines,
			treatmentList(network.treatments),
			studyList(network.studies))
	}

	def apply[M <: Measurement](network: Network[M], tree: Tree[Treatment])
	: NetworkModel[M, ConsistencyParametrization[M]] = {
		val pmtz = new ConsistencyParametrization(network,
			new FundamentalGraphBasis(network.treatmentGraph, tree))
		new NetworkModel[M, ConsistencyParametrization[M]](pmtz,
			assignBaselines(pmtz),
			treatmentList(network.treatments),
			studyList(network.studies))
	}

	def apply[M <: Measurement](network: Network[M], base: Treatment)
	: NetworkModel[M, ConsistencyParametrization[M]] = {
		apply(network, network.minimumDiameterSpanningTree) // FIXME: don't ignore base
	}

	def apply[M <: Measurement](network: Network[M])
	: NetworkModel[M, ConsistencyParametrization[M]] = {
		apply(network, network.minimumDiameterSpanningTree)
	}

	def assignBaselines[M <: Measurement](pmtz: ConsistencyParametrization[M])
	: Map[Study[M], Treatment] = {
		val alg = new DFS()
		alg.search(BaselineSearchProblem(pmtz)) match {
			case None => throw new Exception("No Assignment Found!")
			case Some(x) => x.assignment
		}
	}
}

object NodeSplitNetworkModel extends NetworkModelUtil {
	def apply[M <: Measurement](network: Network[M],
		split: (Treatment, Treatment),
		tree: Tree[Treatment],
		baselines: Map[Study[M], Treatment])
	: NetworkModel[M, NodeSplitParametrization[M]] = {
		val pmtz = new NodeSplitParametrization(network,
			new FundamentalGraphBasis(network.treatmentGraph, tree), split)
		new NetworkModel[M, NodeSplitParametrization[M]](
			pmtz, baselines,
			treatmentList(network.treatments),
			studyList(network.studies))
	}

	def apply[M <: Measurement](network: Network[M],
		split: (Treatment, Treatment), tree: Tree[Treatment])
	: NetworkModel[M, NodeSplitParametrization[M]] = {
		val pmtz = new NodeSplitParametrization(network,
			new FundamentalGraphBasis(network.treatmentGraph, tree), split)
		new NetworkModel[M, NodeSplitParametrization[M]](pmtz,
			assignBaselines(pmtz),
			treatmentList(network.treatments),
			studyList(network.studies))
	}

	def apply[M <: Measurement](network: Network[M],
		split: (Treatment, Treatment))
	:NetworkModel[M, NodeSplitParametrization[M]] = 
		apply(network, split, network.someSpanningTree(split._1, split._2))

	def assignBaselines[M <: Measurement](pmtz: NodeSplitParametrization[M])
	: Map[Study[M], Treatment] = {
		val alg = new DFS()
		alg.search(BaselineSearchProblem(pmtz)) match {
			case None => throw new Exception("No Assignment Found!")
			case Some(x) => x.assignment
		}
	}

	def getSplittableNodes[M <: Measurement](network: Network[M])
	: Set[(Treatment, Treatment)] = {
		val tree = SpanningTreeEnumerator.treeEnumerator(
			network.treatmentGraph, 
			treatmentList(network.treatments).first).head
		val basis = new FundamentalGraphBasis(network.treatmentGraph, tree)

		Set[(Treatment, Treatment)]() ++ (
			if (basis.cycles.isEmpty) {
				Set()
			} else {
				basis.cycles.map(cycle => new Cycle(cycle).edgeSeq.map(order _)
					).reduceLeft((a, b) => a ++ b)
			})
	}

	private def order(e: (Treatment, Treatment)) = {
		if (e._1 < e._2) e
		else (e._2, e._1)
	}
}

object InconsistencyNetworkModel extends NetworkModelUtil {
	def apply[M <: Measurement](network: Network[M], tree: Tree[Treatment])
	: NetworkModel[M, InconsistencyParametrization[M]] = {
		val pmtz = new InconsistencyParametrization(network,
			new FundamentalGraphBasis(network.treatmentGraph, tree))
		new NetworkModel[M, InconsistencyParametrization[M]](pmtz,
			assignBaselines(pmtz),
			treatmentList(network.treatments),
			studyList(network.studies))
	}

	def apply[M <: Measurement](network: Network[M], base: Treatment)
	: NetworkModel[M, InconsistencyParametrization[M]] = {
		apply(network, network.bestSpanningTree(base))
	}

	def apply[M <: Measurement](network: Network[M])
	: NetworkModel[M, InconsistencyParametrization[M]] = {
		apply(network, treatmentList(network.treatments).first)
	}

	def assignBaselines[M <: Measurement](pmtz: InconsistencyParametrization[M])
	: Map[Study[M], Treatment] = {
		val alg = new DFS()
		alg.search(BaselineSearchProblem(pmtz)) match {
			case None => throw new Exception("No Assignment Found!")
			case Some(x) => x.assignment
		}
	}
}

object NetworkModel extends NetworkModelUtil {
}

trait NetworkModelUtil {
	def studyList[M <: Measurement](studies: Set[Study[M]]) = {
		studies.toList.sort((a, b) => a.id < b.id)
	}

	def treatmentList(treatments: Set[Treatment]) = {
		treatments.toList.sort((a, b) => a < b)
	}

	def indexMap[T](l: List[T]): Map[T, Int] =
		Map[T, Int]() ++ l.map(a => (a, l.indexOf(a) + 1))
}
