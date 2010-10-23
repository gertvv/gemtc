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

class Graph[T <% Ordered[T]](edges: Set[(T, T)], vertexPrint: (T) => String) {
	val edgeSet: Set[(T, T)] = edges
	val vertexSet: Set[T] = vertices(edges)

	def this(edges: Set[(T, T)]) = this(edges, (t: T) => t.toString)

	def union(other: Graph[T]) =
		if (other canEqual this) new Graph[T](edgeSet ++ other.edgeSet, vertexPrint)
		else throw new IllegalArgumentException

	def intersection(other: Graph[T]) = 
		if (other canEqual this) new Graph[T](edgeSet ** other.edgeSet, vertexPrint)
		else throw new IllegalArgumentException

	def add(e: (T, T)): Graph[T] = new Graph[T](edgeSet + e, vertexPrint)
	def remove(e: (T, T)): Graph[T] = new Graph[T](edgeSet - e, vertexPrint)

	def remove(es: Set[(T, T)]): Graph[T] = new Graph[T](edgeSet -- es, vertexPrint)

	val edgeVector: List[(T, T)] = asVector(edgeSet)

	def asVector(s: Set[(T, T)]): List[(T, T)] =
		s.toList.sort((a, b) =>
			if (a._1 == b._1) a._2 < b._2
			else a._1 < b._1)

	def incidenceVector(edgeVector: List[(T, T)]): List[Boolean] = {
		edgeVector.map(e => containsEdge(e))
	}

	def containsEdge(e: (T, T)): Boolean = {
		edgeSet.contains(e)
	}

	def edgesFrom(v: T): Set[(T, T)] = edgeSet.filter(e => e._1 == v)
	def edgesTo(v: T): Set[(T, T)] = edgeSet.filter(e => e._2 == v)

	override def equals(other: Any) = other match {
		case that: Graph[_] => 
			(that canEqual this) &&
			this.edgeSet == that.edgeSet
		case _ => false
	}

	def canEqual(other: Any) = other.isInstanceOf[Graph[_]]

	override def hashCode = edgeSet.hashCode

	protected def vertices(edges: Set[(T, T)]): Set[T] = {
		edges.flatMap(e => List(e._1, e._2))
	}

	override def toString = edgeSet.toString

	def directedGraph: Graph[T] = this

	def dotString: String = "digraph G {\n" + edgesStr("->") + "\n}"

	def edgesStr(sep: String): String =
		edgeVector.map(edgeStr(sep)).mkString("\n")

	def edgeStr(sep: String)(edge: (T, T)): String =
		"\t\"" + vertexPrint(edge._1) + "\" " + sep +
		" \"" + vertexPrint(edge._2) + "\""
}

class UndirectedGraph[T <% Ordered[T]](
	edges: Set[(T, T)], vertexPrint: (T) => String)
extends Graph[T](UndirectedGraph.order(edges), vertexPrint) {
	def this(edges: Set[(T, T)]) = this(edges, (t: T) => t.toString)

	override def union(other: Graph[T]): UndirectedGraph[T] =
		if ((this canEqual other) && (other canEqual this))
			new UndirectedGraph[T](edgeSet ++ other.edgeSet, vertexPrint)
		else throw new IllegalArgumentException

	override def intersection(other: Graph[T]): UndirectedGraph[T] =
		if ((this canEqual other) && (other canEqual this))
			new UndirectedGraph[T](edgeSet ** other.edgeSet, vertexPrint)
		else throw new IllegalArgumentException

	override def add(e: (T, T)): UndirectedGraph[T] =
		new UndirectedGraph[T](edgeSet + UndirectedGraph.order(e), vertexPrint)

	override def remove(e: (T, T)): UndirectedGraph[T] =
		new UndirectedGraph[T](edgeSet - UndirectedGraph.order(e), vertexPrint)

	override def remove(es: Set[(T, T)]): UndirectedGraph[T] =
		new UndirectedGraph[T](edgeSet -- UndirectedGraph.order(es), vertexPrint)

	override def containsEdge(e: (T, T)): Boolean =
		super.containsEdge(UndirectedGraph.order(e))

	override def edgesFrom(v: T): Set[(T, T)] =
		super.edgesFrom(v) ++ super.edgesTo(v).map(invert)

	override def edgesTo(v: T): Set[(T, T)] =
		super.edgesTo(v) ++ super.edgesFrom(v).map(invert)

	private def invert(e: (T, T)) = (e._2, e._1)

	override def equals(other: Any) = other match {
		case that: UndirectedGraph[_] =>
			(that canEqual this) &&
			this.edgeSet == that.edgeSet
		case _ => false
	}

	override def canEqual(other: Any) = other.isInstanceOf[UndirectedGraph[_]]

	override def directedGraph: Graph[T] = 
		new Graph[T](
			edgeSet.map(e => Set(e, invert(e))).reduceLeft((a, b) => a ++ b),
			vertexPrint
		)

	/**
	 * Give the fundamental cycles that belong with spanning tree st.
	 */
	def fundamentalCycles(st: Tree[T]): Set[UndirectedGraph[T]] = {
		for (e <- remove(st.edgeSet).edgeSet)
		yield st.createCycle(e)
	}

	override def dotString: String = "graph G {\n" + edgesStr("--") + "\n}"
}

object UndirectedGraph {
	def order[T <% Ordered[T]](e: (T, T)): (T, T) = 
		if (e._1 <= e._2) e
		else (e._2, e._1)

	def order[T <% Ordered[T]](es: Set[(T, T)]): Set[(T, T)] =
		es.map(e => order(e))
}

class Tree[T <% Ordered[T]](edges: Set[(T, T)], val root: T,
		vertexPrint: (T) => String)
extends Graph[T](edges) {
	override val vertexSet = vertices(edges) + root

	def this(edges: Set[(T, T)], root: T) =
		this(edges, root, (t: T) => t.toString)

	override def add(e: (T, T)): Tree[T] = new Tree[T](edgeSet + e, root,
		vertexPrint)

	/**
	 * Given an edge e = (w, v) that is not in this tree and with
	 * w and v in the set of vertices, give the cycle containing e.
	 * Assume the graph to be undirected.
	 */
	def createCycle(e: (T, T)): UndirectedGraph[T] = {
		require(!edgeSet.contains(e))
		require(vertexSet.contains(e._1), vertexSet.contains(e._2))

		val w = e._1
		val v = e._2
		val a = commonAncestor(w, v)

		val c = new UndirectedGraph[T](
			pathEdges(path(a, w)) ++ pathEdges(path(a, v)) + e, vertexPrint)
		if (c.edgeSet.size <= 1) new UndirectedGraph[T](Set[(T, T)]())
		else c
	}

	private def pathEdges(p: List[T]): Set[(T, T)] = {
		def aux(p: List[T]): List[(T, T)] = p match {
			case Nil => Nil
			case x :: Nil => Nil
			case x :: l => (x, l.head) :: aux(l)
		}
		Set[(T, T)]() ++ aux(p)
	}

	/**
	 * Find the cycle completed by (w, v)
	 */
	def cycle(w: T, v: T): List[T] = {
		require(w != v)
		val a = commonAncestor(w, v)
		path(a, w) ++ path(a, v).reverse
	}

	/**
	 * Find the path from w to v, given that they are on the same branch.
	 */
	def path(w: T, v: T): List[T] = {
		def aux(w: T, v: T): List[T] =
			if (w == v) List(v)
			else if (v == root) Nil
			else {
				val p = aux(w, edgesTo(v).toList.head._1)
				if (p == Nil) Nil
				else v :: p
			}
		aux(w, v).reverse
	}

	/**
	 * Find the closest common ancestor of w and v.
	 */
	def commonAncestor(w: T, v: T): T = {
		def aux(l1: List[T], l2: List[T]): Option[T] = {
			if (l1.isEmpty || l2.isEmpty || l1.head != l2.head) None
			else aux(l1.tail, l2.tail) match {
				case None => Some(l1.head)
				case x => x
			}
		}
		aux(path(root, v), path(root, w)) match {
			case None => throw new IllegalStateException
			case Some(u) => u
		}
	}
}

class FundamentalGraphBasis[T <% Ordered[T]](
	val graph: UndirectedGraph[T],
	val tree: Tree[T],
	vertexPrint: (T) => String) {

	def this(
		graph: UndirectedGraph[T],
		tree: Tree[T]) = this(graph, tree, (t: T) => t.toString)
	
	require(tree.vertexSet == graph.vertexSet)

	val treeEdges = tree.edgeSet
	val backEdges = graph.remove(tree.edgeSet).edgeSet
	val cycles = backEdges.map(e => tree.cycle(e._1, e._2))

	def dotString: String = "digraph G {\n" + edgesStr + "\n}"

	def edgesStr = (graph.asVector(treeEdges).map(graph.edgeStr("->")) :::
		graph.asVector(backEdges).map(graph.edgeStr("->")
			).map(s => s + " [style=dashed]")).mkString("\n")
}

class Cycle[T <% Ordered[T]](
	val vertexSeq: List[T]) {

	require(Cycle.isCycle(vertexSeq))

	val edgeSeq: List[(T, T)] = edgeSeq(vertexSeq)

	private def edgeSeq(trail: List[T]): List[(T, T)] = trail match {
		case a :: (b :: l) => (a, b) :: edgeSeq(b :: l)
		case a :: Nil => List()
		case Nil => List()

	}

	override def equals(other: Any) = other match {
		case that: Cycle[T] => { that.vertexSeq == vertexSeq }
		case _ => false
	}

	override def hashCode: Int = vertexSeq.hashCode

	def rebase(base: T): Cycle[T] = new Cycle(rebase(base, vertexSeq.tail, Nil))

	private def rebase(base: T, toWalk: List[T], walked: List[T])
	: List[T] = toWalk match {
		case Nil => throw new IllegalArgumentException
		case x :: l => 
			if (x == base) toWalk ::: (base :: walked).reverse
			else rebase(base, toWalk.tail, toWalk.head :: walked)
	}
}

object Cycle {
	def isCycle[T <% Ordered[T]](vertexSeq: List[T]): Boolean =
		(vertexSeq.size > 3) && (vertexSeq.head == vertexSeq.last) &&
		((Set[T]() ++ vertexSeq).size == (vertexSeq.size - 1))

	def apply[T <% Ordered[T]](vertexSeq: List[T]): Cycle[T] = new Cycle(vertexSeq)
	def apply[T <% Ordered[T]](g: UndirectedGraph[T]): Cycle[T] = {
		val v = first(g.vertexSet)
		val c = walkCycle(g, List(first(reach(g, v)), v)).reverse
		new Cycle(c)
	}

	private def walkCycle[T <% Ordered[T]](g: UndirectedGraph[T], l0: List[T]): List[T] = {
		if (g.edgesFrom(l0.head).size != 2) throw new IllegalArgumentException()
		if (l0.size > 1 && l0.head == l0.last) l0
		else {
			val v = first(reach(g, l0.head) - l0.tail.head)
			walkCycle(g, v :: l0)
		}
	}

	private def first[T <% Ordered[T]](set: Set[T]) = set.toList.sort((a, b) => a < b)(0)

	private def reach[T <% Ordered[T]](g: UndirectedGraph[T], v: T): Set[T] = {
		g.edgesFrom(v).map(x => x._2)
	}
}
