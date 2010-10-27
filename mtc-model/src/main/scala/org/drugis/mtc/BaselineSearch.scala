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

object BaselineSearchProblem {
	def apply[M <: Measurement](network: Network[M]) =
		new BaselineSearchProblem(network, completeConstraint(network)_)

	def apply[M <: Measurement](pmtz: InconsistencyParametrization[M]) =
		new BaselineSearchProblem(pmtz.network, constraint(pmtz)_)

	def apply[M <: Measurement](pmtz: ConsistencyParametrization[M]) =
		new BaselineSearchProblem(pmtz.network, constraint(pmtz)_)

	def apply[M <: Measurement](pmtz: NodeSplitParametrization[M]) =
		new NodeSplitBaselineSearchProblem(pmtz.network, constraint(pmtz)_,
			pmtz.splitNode)

	private def completeConstraint[M <: Measurement](network: Network[M])(
		edges: Set[(Treatment, Treatment)])
	: Boolean = network.treatmentGraph.edgeSet == edges

	private def constraint[M <: Measurement](
		pmtz: NodeSplitParametrization[M])(
		edges: Set[(Treatment, Treatment)])
	: Boolean = {
		pmtz.cycles.forall(cycle => cycleConstraint(cycle, edges))
	}

	private def constraint[M <: Measurement](
		pmtz: ConsistencyParametrization[M])(
		edges: Set[(Treatment, Treatment)])
	: Boolean = {
		pmtz.cycles.forall(cycle => cycleConstraint(cycle, edges))
	}

	private def constraint[M <: Measurement](
		pmtz: InconsistencyParametrization[M])(
		edges: Set[(Treatment, Treatment)])
	: Boolean = {
		pmtz.cycles.forall(cycle => cycleConstraint(cycle, edges)) &&
		pmtz.inconsistencyClasses.forall(cls =>
			inconsConstraint(pmtz.inconsistencyCycles(cls), edges))
		
	}

	/**
	 * Ensure that at least n - 1 edges are covered in every cycle.
	 */
	private def cycleConstraint(cycle: Cycle[Treatment],
		edges: Set[(Treatment, Treatment)])
	: Boolean = notCovered(cycle, edges) <= 1

	/**
	 * Ensure that for each inconsistency class, at least one cycle is fully
	 * covered.
	 */
	private def inconsConstraint(cycles: Set[Cycle[Treatment]],
		edges: Set[(Treatment, Treatment)])
	: Boolean = {
		cycles.map(cycle => notCovered(cycle, edges)).exists(n => n == 0)
	}

	/**
	 * Return the number of edges in the cycle that are not covered by the
	 * given set of edges.
	 */
	private def notCovered(cycle: Cycle[Treatment],
		edges: Set[(Treatment, Treatment)])
	: Int = {
		val cg = new UndirectedGraph(Set[(Treatment, Treatment)]() ++
			cycle.edgeSeq)
		val eg = new UndirectedGraph(edges)
		cg.edgeSet.size - cg.intersection(eg).edgeSet.size
	}
}

class NodeSplitBaselineSearchProblem[M <: Measurement](
	network: Network[M],
	constraint: (Set[(Treatment, Treatment)]) => Boolean,
	splitNode: (Treatment, Treatment))
extends BaselineSearchProblem[M](network, constraint) {
	override def successors(s: BaselineSearchState[M])
	: List[BaselineSearchState[M]] = {
		if (s.studies.isEmpty) Nil
		else {
			val study = s.studies.head
			val treatments =
				if (containsSplitNode(study)) List(splitNode._1, splitNode._2)
				else study.treatments.toList.sort((a, b) => a < b)
			(for {t <- treatments
				val assignment = s.assignment + ((study, t))
			} yield new BaselineSearchState(s.studies.tail, assignment)
			).toList
		}
	}

	private def containsSplitNode(s: Study[M]) =
		s.treatments.contains(splitNode._1) &&
			s.treatments.contains(splitNode._2)
}

class BaselineSearchProblem[M <: Measurement](
	network: Network[M],
	constraint: (Set[(Treatment, Treatment)]) => Boolean)
extends SearchProblem[BaselineSearchState[M]] {

	private val twoArmStudies: Set[Study[M]] = 
		network.studies.filter(study => study.treatments.size == 2)
	private val multiArmStudies: Set[Study[M]] =
		network.studies -- twoArmStudies
	private val initialAssignment: Map[Study[M], Treatment] = 
		Map[Study[M], Treatment]() ++ twoArmStudies.map(study =>
			(study, study.treatments.toList.sort((a, b) => a < b).head))

	val initialState = new BaselineSearchState(
		multiArmStudies.toList,
		initialAssignment)

	def isGoal(s: BaselineSearchState[M]): Boolean = {
		val edges = s.coverGraph.edgeSet
		s.studies.isEmpty && constraint(edges)
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
