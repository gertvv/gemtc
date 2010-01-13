package org.drugis

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
}
