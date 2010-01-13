package org.drugis

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

class SpanningTreeEnumeratorTest extends ShouldMatchersForJUnit {
	@Test def testEnumerateDirected() {
		val g = new Graph[String](Set[(String, String)](
				("A", "B"), ("A", "C"), ("B", "D"), ("D", "C"),
				("B", "C"), ("C", "B")
			))

		val t1 = new Graph[String](Set[(String, String)](
				("A", "B"), ("B", "D"), ("D", "C")
			))

		val t2 = new Graph[String](Set[(String, String)](
				("A", "B"), ("B", "D"), ("B", "C")
			))

		val t3 = new Graph[String](Set[(String, String)](
				("A", "B"), ("B", "D"), ("A", "C")
			))

		val t4 = new Graph[String](Set[(String, String)](
				("A", "C"), ("C", "B"), ("B", "D")
			))

		val ts = Set[Graph[String]](t1, t2, t3, t4)

		val found = SpanningTreeEnumerator.treeEnumerator(g, "A").toList
		(Set[Graph[String]]() ++ found).size should be (found.size)
		(Set[Graph[String]]() ++ found) should be (ts)
	}

	@Test def testEnumerateUndirected() {
		val g = new UndirectedGraph[String](Set[(String, String)](
				("A", "B"), ("A", "C"), ("A", "D"),
				("B", "C"), ("B", "D"),
				("C", "D")
			))

		val found = SpanningTreeEnumerator.treeEnumerator(g, "A").toList
		(Set[Graph[String]]() ++ found).size should be (found.size)
		found.size should be (16)
	}
}
