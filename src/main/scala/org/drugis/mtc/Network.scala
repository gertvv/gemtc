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

class Network[M <: Measurement](
		val treatments: Set[Treatment], val studies: Set[Study[M]]) {
	override def toString = treatments.toString + studies.toString

	val measurementType: Class[M] =
		studies.find(_ => true) match {
			case Some(x) => x.measurements.values.find(_ => true) match {
				case Some(y) => y.getClass.asInstanceOf[Class[M]]
				case None => null
			}
			case None => null
		}

	val measurementTypeString: String = {
		if (measurementType == classOf[DichotomousMeasurement])
			"dichotomous"
		else if (measurementType == classOf[ContinuousMeasurement])
			"continuous"
		else if (measurementType == classOf[NoneMeasurement])
			"none"
		else if (measurementType == null)
			"none"
		else throw new IllegalStateException("Unknown type " + measurementType)
	}

	val treatmentGraph: UndirectedGraph[Treatment] = {
		var graph = new UndirectedGraph[Treatment](
			Set[(Treatment, Treatment)](), (t: Treatment) => t.id)
		for (s <- studies) {
			graph = graph.union(s.treatmentGraph)
		}
		graph
	}

	private def invert(e: (Treatment, Treatment)) = (e._2, e._1)

	def supportingStudies(edge: (Treatment, Treatment)): Set[Study[M]] = {
		studies.filter(s => s.treatmentGraph.containsEdge(edge))
	}

	def evidenceDimensionality(cycle: UndirectedGraph[Treatment]): Int = {
		Partition(this, Cycle(cycle)).reduce.parts.size
	}

	def isInconsistency(cycle: UndirectedGraph[Treatment]): Boolean =
		evidenceDimensionality(cycle) >= 3

	val edgeVector: List[(Treatment, Treatment)] =
		treatmentGraph.edgeVector

	val countFunctionalParameters: Int =
		treatmentGraph.edgeSet.size - treatmentGraph.vertexSet.size + 1

	def countInconsistencies(st: Tree[Treatment]): Int =
		inconsistencies(st).size

	def inconsistencies(st: Tree[Treatment]) : Set[UndirectedGraph[Treatment]] = {
		for {c <- treatmentGraph.fundamentalCycles(st);
			if isInconsistency(c)} yield c
	}

	def treeEnumerator(top: Treatment) =
		SpanningTreeEnumerator.treeEnumerator(treatmentGraph, top)

	private def compare(a: Tree[Treatment], b: Tree[Treatment]): Int = {
		countInconsistencies(a) - countInconsistencies(b)
	}

	private def better(a: Tree[Treatment], b: Tree[Treatment]): Boolean = {
		compare(a, b) > 0
	}

	def bestSpanningTree(top: Treatment): Tree[Treatment] = {
		searchSpanningTree(top, new NullSpanningTreeSearchListener())
	}

	private def baselineAssignmentExists(pmtz: Parametrization[M]): Boolean =
		try {
			NetworkModel.assignBaselines(pmtz)
			true
		} catch {
			case _ => false
		}

	private def completeBaselineAssignment = 
		(new DFS()).search(BaselineSearchProblem(this))

	def searchSpanningTree(top: Treatment, l: SpanningTreeSearchListener)
	: Tree[Treatment] = {
		// First, check if there is an assignment that covers the full graph
		val completeBaseline = completeBaselineAssignment
		val hasCompleteBaseline = completeBaseline match {
			case Some(x) => true
			case None => false
		}
		val maxPossible =
			if (hasCompleteBaseline) countFunctionalParameters
			else countFunctionalParameters - 1

		var max = 0
		var best: Tree[Treatment] = null
		for (tree <- treeEnumerator(top)) {
			val pmtz = new Parametrization(this, 
				new FundamentalGraphBasis(treatmentGraph, tree))
			val incons = pmtz.inconsistencyDegree
			if (incons >= max) {
				// Optimization for ICD(T) = K case
				val hasBaseline = 
					if (hasCompleteBaseline) true
					else if (incons == countFunctionalParameters) false
					else baselineAssignmentExists(pmtz)
				if (hasBaseline) {
					max = incons
					best = tree
					l.receive(tree, incons, true, Some(true), true)
				} else {
					l.receive(tree, incons, true, Some(false), false)
				}
			} else {
				l.receive(tree, incons, false, None, false)
			}
			if (max == maxPossible) {
				return best
			}
		}
		best
	}

	def filterTreatments(ts: Set[Treatment]): Network[M] = {
		if (!ts.subsetOf(treatments))
			throw new RuntimeException(ts + " not a subset of " + treatments)

		new Network[M](ts,
			studies.filter(s => s.treatments.intersect(ts).size > 1).map(
				s => filterStudy(s, ts)))
	}

	private def filterStudy(s: Study[M], ts: Set[Treatment]): Study[M] =
		if (s.treatments.intersect(ts) == s.treatments) s
		else new Study[M](s.id, s.measurements.filter(x => ts.contains(x._1)))

	def study(id: String) = studies.find(s => s.id == id) match {
		case Some(s) => s
		case None => null
	}

	def treatment(id: String) = treatments.find(t => t.id == id) match {
		case Some(t) => t
		case None => null
	}

	def toXML = <network type={measurementTypeString}>
		<treatments>{treatments.toList.sort((a, b) => a.id < b.id).map(t => t.toXML)}</treatments>
		<studies>{studies.toList.sort((a, b) => a.id < b.id).map(s => s.toXML)}</studies>
	</network>

	def toPrettyXML =
		(new scala.xml.PrettyPrinter(80, 4)).format(toXML)
}

object Network {
	/*
	def fromXML(node: scala.xml.Node): Network[DichotomousMeasurement] = {
		dichFromXML(node)
	} */
	def fromXML(node: scala.xml.Node): Network[_ <: Measurement] = {
		node.attribute("type") match {
			case Some(x) => x.text match {
				case "continuous" => contFromXML(node)
				case "dichotomous" => dichFromXML(node)
				case "none" => noneFromXML(node)
				case _ => throw new RuntimeException(
					"Unsupported network type " + x.text)
			}
			case None => dichFromXML(node)
		}
	}

	def dichFromXML(node: scala.xml.Node): Network[DichotomousMeasurement] = {
		val treatments = treatmentsFromXML((node \ "treatments")(0))
		new Network(Set[Treatment]() ++ treatments.values, 
			studiesFromXML((node \ "studies")(0), treatments,
				DichotomousMeasurement.fromXML))
	}

	def contFromXML(node: scala.xml.Node): Network[ContinuousMeasurement] = {
		val treatments = treatmentsFromXML((node \ "treatments")(0))
		new Network(Set[Treatment]() ++ treatments.values, 
			studiesFromXML((node \ "studies")(0), treatments,
				ContinuousMeasurement.fromXML))
	}

	def noneFromXML(node: scala.xml.Node): Network[NoneMeasurement] = {
		val treatments = treatmentsFromXML((node \ "treatments")(0))
		new Network(Set[Treatment]() ++ treatments.values, 
			studiesFromXML((node \ "studies")(0), treatments,
				NoneMeasurement.fromXML))
	}

	def treatmentsFromXML(n: scala.xml.Node): Map[String, Treatment] =
		Map[String, Treatment]() ++
		{for (node <- n \ "treatment") yield ((node \ "@id").text, Treatment.fromXML(node))}

	def studiesFromXML[M <: Measurement](n: scala.xml.Node,
			treatments: Map[String, Treatment],
			measReader: (scala.xml.Node, Map[String, Treatment]) => M)
	: Set[Study[M]] =
		Set[Study[M]]() ++
		{for (node <- n \ "study") yield Study.fromXML(node, treatments, measReader)}
}

trait SpanningTreeSearchListener {
	def receive(tree: Tree[Treatment], icd: Integer, max: Boolean,
		assignment: Option[Boolean], best: Boolean): Unit
}

class NullSpanningTreeSearchListener extends SpanningTreeSearchListener {
	def receive(tree: Tree[Treatment], icd: Integer, max: Boolean,
		assignment: Option[Boolean], best: Boolean) {}
}
