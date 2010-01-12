package org.drugis

object SpanningTreeEnumerator {
	/**
	 * Enumerate all spanning trees of g rooted at r.
	 */
	def treeEnumerator[T](g: Graph[T], r: T): Iterable[Graph[T]] = null

	/**
	 * Enumerate all spanning trees of the undirected graph g.
	 */
	def treeEnumerator[T](g: UndirectedGraph[T]): Iterable[Graph[T]] = null

	private def grow[T](g: Graph[T], t: Graph[T], f: List[(T, T)],
			complete: Graph[T] => Boolean) = {
		if (complete(t)) List(t)
		else {
			val d = deepen(g, t, f.tail, f.head, complete)
			val l = d.head // last generated tree
			if (bridgeTest(l, e)) d
			else grow(g.remove(e), t, f.tail) :: d
		}
	}

	private def deepen(g: Graph[T], t: Graph[T], f: List[(T, T)], e: (T, T),
			complete: Graph[T] => Boolean) = {
		val v = e._1
		val t1 = t.add(e)
		val f1 = (g.edgesFrom(v).filter(x => !t.vertexSet.contains(x._1)) ::: f).removeAll(x => x._2 == v)
		grow(g, t1, f1, complete)
	}

	private def bridgeTest(t: Graph[T], e: (T, T)) = {
		// this is fucked
	}
}
