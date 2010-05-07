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

object SpanningTreeEnumerator {
	/**
	 * Enumerate all spanning trees of g rooted at r.
	 */
	def treeEnumerator[T <% Ordered[T]](g: Graph[T], r: T)
	: Iterable[Tree[T]] = 
		new SpanningTreeIterable[T](g, r)

	/**
	 * Enumerate all spanning trees of the undirected graph g.
	 */
	def treeEnumerator[T <% Ordered[T]](g: UndirectedGraph[T])
	:Iterable[Tree[T]] =
	g.vertexSet.find(_ => true) match {
			case None => Nil
			case Some(r) => treeEnumerator(g, r)
	}
}

class SpanningTreeIterable[T <% Ordered[T]](
		val graph: Graph[T], val root: T)
extends Iterable[Tree[T]] {
	def elements: Iterator[Tree[T]] = new SpanningTreeIterator[T](graph, root)
}

/**
 * Iterate over all spanning trees of a graph, storing only 2 at a time.
 */
class SpanningTreeIterator[T <% Ordered[T]](
		val graph: Graph[T], val root: T)
extends Iterator[Tree[T]] {
	private var last: Tree[T] = null
	private var current: Tree[T] = null
	private var queue: List[ProblemState] = List(new ProblemState(
			graph.directedGraph, // algorithm requires a directed graph
			new Tree[T](Set[(T, T)](), root), // the result tree so far
			graph.edgesFrom(root).toList, // the fringe of unexplored edges
			false // don't do bridge test
		))

	def next: Tree[T] = {
		if (hasNext) {
			last = current
			current = null
			last
		} else null
	}

	def hasNext: Boolean = {
		if (current == null) current = findNext
		current != null
	}

	/**
	 * Find the next spanning tree.
	 */
	private def findNext: Tree[T] = {
		while (!queue.isEmpty) {
			val head = queue.head
			queue = successors(head) ::: queue.tail
			if (isGoal(head)) return head.tree
		}
		null
	}

	/**
	 * Given the current state, determine the next state(s).
	 */
	private def successors(state: ProblemState): List[ProblemState] = {
		if (state.fringe.isEmpty) Nil
		else if (state.bridgeTest) {
			widenSucc(state)
		} else {
			deepenSucc(state) ::: bridgeTestSucc(state)
		}
	}

	/**
	 * Widen the search.
	 */
	private def widenSucc(state: ProblemState): List[ProblemState] = {
		val e = state.fringe.head
		val g1 = state.graph.remove(e)
		if (bridgeTest(g1, e._2)) Nil
		else List(new ProblemState(g1, state.tree, state.fringe.tail, false))
	}

	/**
	 * Determine whether to widen the search (if e is a bridge, all spanning
	 * trees will include it, hence we don't need to widen the search).
	 */
	private def bridgeTestSucc(state: ProblemState): List[ProblemState] = {
		List(new ProblemState(state.graph, state.tree, state.fringe, true))
	}

	/**
	 * Search deeper.
	 */
	private def deepenSucc(state: ProblemState): List[ProblemState] = {
		val e = state.fringe.head
		val v = e._2
		val t1 = state.tree.add(e)
		val f = state.fringe.tail
		val g = state.graph
		// Add to the fringe forall_w (v, w) \in g, w \not\in t
		// Then, remove \forall_w (w, v) from F
		val f1 = (
			g.edgesFrom(v).filter(
				x => !t1.vertexSet.contains(x._2)
			).toList ::: f).filter(
				x => x._2 != v
			)
		return List(new ProblemState(g, t1, f1, false))
	}

	/**
	 * Test whether the given state is a goal state (spanning tree found).
	 */
	private def isGoal(state: ProblemState): Boolean = 
		!state.bridgeTest && complete(state.tree)

	/**
	 * Test whether v is a bridge in g, using the last found spanning tree.
	 */
	private def bridgeTest(g: Graph[T], v: T) =
		if (last == null) throw new IllegalStateException
		else !g.edgesTo(v).map(x => x._1).exists(w => !descendent(last, v, w))

	/**
	 * Represents a node on the search queue.
	 */
	private class ProblemState(
		val graph: Graph[T],
		val tree: Tree[T],
		val fringe: List[(T, T)],
		val bridgeTest: Boolean) { }

	/**
	 * Test whether t is a complete tree for graph.
	 */
	private def complete(t: Tree[T]): Boolean = t.vertexSet == graph.vertexSet

	/**
	 * Test whether w is a descendent of v in tree t.
	 */
	private def descendent[T](t: Tree[T], v: T, w: T): Boolean = {
		if (v == w) true
		else t.edgesFrom(v).map(x => x._2).exists(u => descendent(t, u, w))
	}
}
