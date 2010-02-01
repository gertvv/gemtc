package org.drugis.mtc

class Graph[T <% Ordered[T]](edges: Set[(T, T)]) {
	val edgeSet: Set[(T, T)] = edges
	val vertexSet: Set[T] = vertices(edges)

	def union(other: Graph[T]) =
		if (other canEqual this) new Graph[T](edgeSet ++ other.edgeSet)
		else throw new IllegalArgumentException

	def intersection(other: Graph[T]) = 
		if (other canEqual this) new Graph[T](edgeSet ** other.edgeSet)
		else throw new IllegalArgumentException

	def add(e: (T, T)): Graph[T] = new Graph[T](edgeSet + e)
	def remove(e: (T, T)): Graph[T] = new Graph[T](edgeSet - e)

	def remove(es: Set[(T, T)]): Graph[T] = new Graph[T](edgeSet -- es)

	def edgeVector: List[(T, T)] =
		edgeSet.toList.sort((a, b) =>
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
}

class UndirectedGraph[T <% Ordered[T]](edges: Set[(T, T)])
extends Graph[T](UndirectedGraph.order(edges)) {

	override def union(other: Graph[T]): UndirectedGraph[T] =
		if ((this canEqual other) && (other canEqual this))
			new UndirectedGraph[T](edgeSet ++ other.edgeSet)
		else throw new IllegalArgumentException

	override def intersection(other: Graph[T]): UndirectedGraph[T] =
		if ((this canEqual other) && (other canEqual this))
			new UndirectedGraph[T](edgeSet ** other.edgeSet)
		else throw new IllegalArgumentException

	override def add(e: (T, T)): UndirectedGraph[T] =
		new UndirectedGraph[T](edgeSet + UndirectedGraph.order(e))

	override def remove(e: (T, T)): UndirectedGraph[T] =
		new UndirectedGraph[T](edgeSet - UndirectedGraph.order(e))

	override def remove(es: Set[(T, T)]): UndirectedGraph[T] =
		new UndirectedGraph[T](edgeSet -- UndirectedGraph.order(es))

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
			edgeSet.map(e => Set(e, invert(e))).reduceLeft((a, b) => a ++ b)
		)

	/**
	 * Give the fundamental cycles that belong with spanning tree st.
	 */
	def fundamentalCycles(st: Tree[T]): Set[UndirectedGraph[T]] = {
		for (e <- remove(st.edgeSet).edgeSet)
		yield st.createCycle(e)
	}
}

object UndirectedGraph {
	def order[T <% Ordered[T]](e: (T, T)): (T, T) = 
		if (e._1 <= e._2) e
		else (e._2, e._1)

	def order[T <% Ordered[T]](es: Set[(T, T)]): Set[(T, T)] =
		es.map(e => order(e))
}

class Tree[T <% Ordered[T]](edges: Set[(T, T)], val root: T)
extends Graph[T](edges) {
	override val vertexSet = vertices(edges) + root

	override def add(e: (T, T)): Tree[T] = new Tree[T](edgeSet + e, root)

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
			pathEdges(path(a, w)) ++ pathEdges(path(a, v)) + e)
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
	val tree: Tree[T]) {
	
	require(tree.vertexSet == graph.vertexSet)

	val treeEdges = tree.edgeSet
	val backEdges = graph.remove(tree.edgeSet).edgeSet
	val cycles = backEdges.map(e => tree.cycle(e._1, e._2))
}
