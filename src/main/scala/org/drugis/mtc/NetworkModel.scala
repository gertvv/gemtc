/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2010 Gert van Valkenhoef.
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

trait NetworkModelParameter {

}

final class BasicParameter(val base: Treatment, val subject: Treatment)
extends NetworkModelParameter {
	override def toString() = "d." + base.id + "." + subject.id

	override def equals(other: Any): Boolean = other match {
		case p: BasicParameter => (p.base == base && p.subject == subject)
		case _ => false
	}

	override def hashCode: Int = 31 * base.hashCode + subject.hashCode
}

final class InconsistencyParameter(val cycle: List[Treatment])
extends NetworkModelParameter {
	def this(list: java.util.List[Treatment]) {
		this(scala.collection.jcl.Conversions.convertList(list).toList)
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
class NetworkModel[M <: Measurement](
	val network: Network[M], 
	val basis: FundamentalGraphBasis[Treatment],
	val studyBaseline: Map[Study[M], Treatment],
	val treatmentList: List[Treatment],
	val studyList: List[Study[M]]) {

	require(Set[Study[M]]() ++ studyList == network.studies)
	require(Set[Treatment]() ++ treatmentList == network.treatments)
	require(studyBaseline.keySet == network.studies)
	require(basis.tree.vertexSet == network.treatments)

	val studyMap = NetworkModel.indexMap(studyList)
	val treatmentMap = NetworkModel.indexMap(treatmentList)

	val data = studyList.flatMap(
		study => NetworkModel.treatmentList(study.treatments).map(
				t => (study, study.measurements(t))))

	private val basicParameterEdges = sort(basis.treeEdges)
	private val inconsistencyParameterEdges = sort(inconsistencyEdges)

	private val parameterMap = 
		Map[(Treatment, Treatment), NetworkModelParameter]() ++
		basicParameterEdges.map(e => (e, new BasicParameter(e._1, e._2))) ++
		inconsistencyParameterEdges.map(e => (e, new InconsistencyParameter(
			basis.tree.cycle(e._1, e._2))))

	private val parameterEdges =
		basicParameterEdges ++ inconsistencyParameterEdges

	/**
	 * Gives a list of edges (in the tree) that result directly in parameters
	 * in an ordered manner.
	 */
	val parameterVector: List[NetworkModelParameter] = 
		parameterEdges.map(e => parameterMap(e))

	/**
	 * Gives the list of Inconsistency parameters
	 */
	val inconsistencyParameters: List[InconsistencyParameter] =
		for {param <- parameterVector;
			if (param.isInstanceOf[InconsistencyParameter])
		} yield param.asInstanceOf[InconsistencyParameter];

	/**
	 * Gives the list of Basic parameters
	 */
	val basicParameters: List[BasicParameter] =
		for {param <- parameterVector;
			if (param.isInstanceOf[BasicParameter])
		} yield param.asInstanceOf[BasicParameter];

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

	def parameterization(a: Treatment, b: Treatment)
	: Map[NetworkModelParameter, Int] = param(a, b)

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
		2 * iqr(means)
	}

	private def iqr(x: List[Double]): Double = {
		// Percentile implementation corresponds to type=6 quantile in R
		val p25 = new Percentile(25)
		val p75 = new Percentile(75)
		p75.evaluate(x.toArray) - p25.evaluate(x.toArray)
	}

	private def dichMeans(): List[Double] = {
		for {m <- data} yield
			logOdds(m._2.asInstanceOf[DichotomousMeasurement])
	}

	private def logOdds(m: DichotomousMeasurement): Double = {
		val p = m.responders.toDouble / m.sampleSize.toDouble
		Math.log(p / (1 - p))
	}

	private def contMeans(): List[Double] = {
		for {m <- data} yield
			m._2.asInstanceOf[ContinuousMeasurement].mean
	}

	private def param(a: Treatment, b: Treatment)
	: Map[NetworkModelParameter, Int] = {
		if (basicParameterEdges contains (a, b)) basicParam(a, b)
		else if (basicParameterEdges contains (b, a)) negate(basicParam(b, a))
		else functionalParam(a, b)
	}

	private def functionalParam(a: Treatment, b: Treatment)
	: Map[NetworkModelParameter, Int] = {
		val r = basis.tree.commonAncestor(a, b)
		add(add(negate(pathParam(basis.tree.path(r, a))),
			pathParam(basis.tree.path(r, b))),
			inconsParam(a, b))
	}

	private def inconsParam(a: Treatment, b: Treatment)
	: Map[NetworkModelParameter, Int] = {
		val edges = inconsistencyParameterEdges
		if (edges contains (a, b)) basicParam(a, b)
		else if (edges contains (b, a)) negate(basicParam(b, a))
		else emptyParam()
	}

	private def pathParam(path: List[Treatment])
	: Map[NetworkModelParameter, Int] = {
		if (path.size < 2) emptyParam()
		else add(param(path(0), path(1)), pathParam(path.tail))
	}

	private def basicParam(a: Treatment, b: Treatment) =
		Map[NetworkModelParameter, Int]((parameterMap((a, b)), 1))

	private def negate(p: Map[NetworkModelParameter, Int]) =
		p.transform((a, b) => -b)

	private def add(p: Map[NetworkModelParameter, Int],
			q: Map[NetworkModelParameter, Int])
	: Map[NetworkModelParameter, Int] = {
		emptyParam() ++
		(for {x <- (p.keySet ++ q.keySet)
		} yield (x, getOrZero(p, x) + getOrZero(q, x)))
	}

	private def emptyParam() = Map[NetworkModelParameter, Int]()

	private def getOrZero(p: Map[NetworkModelParameter, Int],
			x: NetworkModelParameter): Int =
	p.get(x) match {
		case None => 0
		case Some(d) => d
	}

	private def inconsistencyEdges: Set[(Treatment, Treatment)] = 
		basis.backEdges.filter(
			e => network.isInconsistency(basis.tree.createCycle(e)))

	private def sort(edges: Set[(Treatment, Treatment)])
	: List[(Treatment, Treatment)] = {
		edges.toList.sort((a, b) => if (a._1 == b._1) a._2 < b._2 else a._1 < b._1)
	}
}

class BaselineSearchState[M <: Measurement](
	val studies: List[Study[M]],
	val assignment: Map[Study[M], Treatment]) {


	def coverGraph: UndirectedGraph[Treatment] = {
		if (assignment.keySet.size == 0)
			new UndirectedGraph(Set[(Treatment,Treatment)]())
		else if (assignment.keySet.size == 1)
			assignment.keySet.map(studyBaselineGraph).toList(0)
		else
			assignment.keySet.map(studyBaselineGraph).reduceLeft(
				(a, b) => a.union(b))
	}

	private def studyBaselineGraph(s: Study[M]): UndirectedGraph[Treatment] = {
		val baseline = assignment(s)
		val other = s.treatments - baseline
		new UndirectedGraph(other.map(x => (baseline, x)))
	}

	override def toString: String = {
		"Remaining: " + studies + "\nAssigned: " + assignment
	}
}

class BaselineSearchProblem[M <: Measurement](
	studies: Set[Study[M]],
	initialAssignment: Map[Study[M], Treatment],
	constraints: List[(Set[(Treatment, Treatment)]) => Boolean])
extends SearchProblem[BaselineSearchState[M]] {

	val initialState = new BaselineSearchState(studies.toList,
		initialAssignment)

	def isGoal(s: BaselineSearchState[M]): Boolean = {
		val edges = s.coverGraph.edgeSet
		s.studies.isEmpty && constraints.forall(c => c(edges))
	}

	def successors(s: BaselineSearchState[M]): List[BaselineSearchState[M]] = {
		if (s.studies.isEmpty) Nil
		else {
			val study = s.studies.head
			(for {t <- study.treatments.toList.sort((a, b) => a < b)
				val assignment = s.assignment + ((study, t))
			} yield new BaselineSearchState(s.studies.tail, assignment)
			).toList
		}
	}
}

object NetworkModel {
	def apply[M <: Measurement](network: Network[M], tree: Tree[Treatment])
	: NetworkModel[M] = {
		new NetworkModel[M](network,
			new FundamentalGraphBasis(network.treatmentGraph, tree),
			assignBaselines(network, tree),
			treatmentList(network.treatments),
			studyList(network.studies))
	}

	def apply[M <: Measurement](network: Network[M], base: Treatment)
	: NetworkModel[M] = {
		apply(network, network.bestSpanningTree(base))
	}

	def apply[M <: Measurement](network: Network[M]): NetworkModel[M] = {
		apply(network, treatmentList(network.treatments).first)
	}

	private def assignMultiArm[M <: Measurement](studies: Set[Study[M]],
		assignment: Map[Study[M], Treatment],
		constraints: List[(Set[(Treatment, Treatment)]) => Boolean])
	: Map[Study[M], Treatment] = {
		val problem = new BaselineSearchProblem(
			studies, assignment, constraints)
		val alg = new DFS()
		alg.search(problem) match {
			case None => throw new Exception("No Assignment Found!")
			case Some(x) => x.assignment
		}
	}

	private def constraint[M <: Measurement](network: Network[M],
			cycle: UndirectedGraph[Treatment])(
			edges: Set[(Treatment, Treatment)])
	: Boolean = {
		val n = cycle.edgeSet.size
		val m =
			if (network.isInconsistency(cycle)) n
			else n - 1
		cycle.intersection(new UndirectedGraph(edges)).edgeSet.size >= m
	}

	def assignBaselines[M <: Measurement](
			network: Network[M], st: Tree[Treatment])
	: Map[Study[M], Treatment] = {
		val twoArm = network.studies.filter(study => study.treatments.size == 2)
		val multiArm = network.studies -- twoArm

		val twoArmMap = Map[Study[M], Treatment]() ++ twoArm.map(study => (study, study.treatments.toList.sort((a, b) => a < b).head))

		val constraints =
			network.treatmentGraph.fundamentalCycles(st).toList.map(
				c => constraint(network, c)_)

		assignMultiArm(multiArm, twoArmMap, constraints)
	}

	def studyList[M <: Measurement](studies: Set[Study[M]]) = {
		studies.toList.sort((a, b) => a.id < b.id)
	}

	def treatmentList(treatments: Set[Treatment]) = {
		treatments.toList.sort((a, b) => a < b)
	}

	def indexMap[T](l: List[T]): Map[T, Int] =
		Map[T, Int]() ++ l.map(a => (a, l.indexOf(a) + 1))
}
