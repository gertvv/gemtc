package org.drugis

object SpanningTreeEnumerator {
	/**
	 * Enumerate all spanning trees of g rooted at r.
	 */
	def treeEnumerator[T <% Ordered[T]](g: Graph[T], r: T)
	: Iterable[Tree[T]] = 
		grow(g.directedGraph, // algorithm requires a directed graph
			new Tree[T](Set[(T, T)](), r), // the result tree so far
			g.edgesFrom(r).toList, // the fringe of nodes
			None, // the last found tree
			(t : Tree[T]) => t.vertexSet == g.vertexSet // complete(t)
		)

	/**
	 * Enumerate all spanning trees of the undirected graph g.
	 */
	def treeEnumerator[T <% Ordered[T]](g: UndirectedGraph[T])
	:Iterable[Tree[T]] =
	g.vertexSet.find(_ => true) match {
			case None => Nil
			case Some(r) => treeEnumerator(g, r)
	}

	/**
	 * Enumerate all spanning trees of g (root: r) containing t (root: r)
	 */
	private def grow[T](g: Graph[T], t: Tree[T], f: List[(T, T)],
			l: Option[Tree[T]],
			complete: Tree[T] => Boolean): List[Tree[T]] = {
		if (complete(t)) List(t)
		else if (f.isEmpty) Nil
		else {
			val d = deepen(g, t, f.tail, f.head, l, complete)
			val l1 = // last generated tree
				if (d.isEmpty) l
				else Some(d.head)
			val e = f.head
			val g1 = g.remove(e)
			if (bridgeTest(g1, l1, e._2)) d
			else grow(g1, t, f.tail, l1, complete) ::: d
		}
	}

	/**
	 * Enumerate all spanning trees in g that contain t + e
	 */
	private def deepen[T](g: Graph[T], t: Tree[T], f: List[(T, T)], e: (T, T),
			l: Option[Tree[T]],
			complete: Tree[T] => Boolean): List[Tree[T]] = {
		val v = e._2
		val t1 = t.add(e)
		val f1 = (g.edgesFrom(v).filter(x => !t1.vertexSet.contains(x._2) && !f.contains(x)).toList ::: f).filter(x => x._2 != v || !t1.vertexSet.contains(x._1))
		grow(g, t1, f1, l, complete)
	}

	private def bridgeTest[T](g: Graph[T], l: Option[Tree[T]], v: T) =
	l match {
		case None => throw new IllegalStateException
		case Some(t) =>
			!g.edgesTo(v).map(x => x._1).exists(w => !descendent(t, v, w))
	}

	/**
	 * Test whether w is a descendent of v in tree t.
	 */
	private def descendent[T](t: Tree[T], v: T, w: T): Boolean = {
		if (v == w) true
		else t.edgesFrom(v).map(x => x._2).exists(u => descendent(t, u, w))
	}
}
