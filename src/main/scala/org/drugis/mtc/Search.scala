package org.drugis.mtc 

trait SearchProblem[State] {
	def initialState: State
	def isGoal(s: State): Boolean
	def successors(s: State): List[State]
}

trait SearchAlgorithm {
	/**
	 * Given the problem, find the solution state
	 * @return The solution, or None if no solution found
	 */
	def search[State](problem: SearchProblem[State]): Option[State]
}

trait GeneralSearch extends SearchAlgorithm {
	var nodesExpanded: Int = 0
	var maxQueueLength: Int = 0

	protected def qfn[State](queue: List[State], additional: List[State]): List[State]

	def search[State](problem: SearchProblem[State]): Option[State] = {
		initStats()
		search(problem, List(problem.initialState))
	}

	def search[State](problem: SearchProblem[State], queue: List[State]): Option[State] = {
		if (queue.isEmpty) return None
		updateStats(queue)
		val fringe = queue.head
		if (problem.isGoal(fringe)) Some(fringe)
		else search(problem, qfn(queue.tail, problem.successors(fringe)))
	}

	private def initStats() = {
		nodesExpanded = 0
		maxQueueLength = 0
	}

	private def updateStats(queue: List[Any]) = {
		nodesExpanded += 1
		if (queue.size > maxQueueLength) maxQueueLength = queue.size
	}
}

class BFS extends GeneralSearch {
	def qfn[State](queue: List[State], additional: List[State]): List[State] = {
		queue ::: additional
	}
}

class DFS extends GeneralSearch {
	def qfn[State](queue: List[State], additional: List[State]): List[State] = {
		additional ::: queue
	}
}
