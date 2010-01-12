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
}
