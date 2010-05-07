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
