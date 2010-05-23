package org.drugis.mtc

object BaselineSearchProblem {
	def apply[M <: Measurement](network: Network[M]) =
		new BaselineSearchProblem(network, completeConstraint(network)_)

	def apply[M <: Measurement](pmtz: Parametrization[M]) =
		new BaselineSearchProblem(pmtz.network, constraint(pmtz)_)

	private def completeConstraint[M <: Measurement](network: Network[M])(
		edges: Set[(Treatment, Treatment)])
	: Boolean = network.treatmentGraph.edgeSet == edges

	private def constraint[M <: Measurement](pmtz: Parametrization[M])(
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
