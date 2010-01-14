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

		val t1 = new Tree[String](Set[(String, String)](
				("A", "B"), ("B", "D"), ("D", "C")
			), "A")

		val t2 = new Tree[String](Set[(String, String)](
				("A", "B"), ("B", "D"), ("B", "C")
			), "A")

		val t3 = new Tree[String](Set[(String, String)](
				("A", "B"), ("B", "D"), ("A", "C")
			), "A")

		val t4 = new Tree[String](Set[(String, String)](
				("A", "C"), ("C", "B"), ("B", "D")
			), "A")

		val ts = Set[Tree[String]](t1, t2, t3, t4)

		val found = SpanningTreeEnumerator.treeEnumerator(g, "A").toList
		(Set[Tree[String]]() ++ found).size should be (found.size)
		(Set[Tree[String]]() ++ found) should be (ts)
	}

	@Test def testEnumerateUndirected() {
		val g = new UndirectedGraph[String](Set[(String, String)](
				("A", "B"), ("A", "C"), ("A", "D"),
				("B", "C"), ("B", "D"),
				("C", "D")
			))

		val found = SpanningTreeEnumerator.treeEnumerator(g, "A").toList
		(Set[Tree[String]]() ++ found).size should be (found.size)
		found.size should be (16)
	}
}
