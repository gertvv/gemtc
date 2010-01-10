package org.drugis

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

class GraphTest extends ShouldMatchersForJUnit {
	@Test def testEdgeSet() {
		val edges = Set[(String, String)](("A", "B"), ("B", "C"))
		new Graph[String](edges).edgeSet should be (edges)
	}

	@Test def testVertexSet() {
		val edges = Set[(String, String)](("A", "B"), ("B", "C"))
		new Graph[String](edges).vertexSet should be
			(Set[String]("A", "B", "C"))
	}

	@Test def testEdgeVector() {
		val edges = Set[(String, String)](("A", "B"), ("B", "C"), ("A", "C"))
		val vector = List[(String, String)](("A", "B"), ("A", "C"), ("B", "C"))
		new Graph[String](edges).edgeVector should be (vector)
	}

	@Test def testIncidenceVector() {
		val graph = new Graph[String](
			Set[(String, String)](("A", "B"), ("B", "C"), ("A", "C")))
		val edgeVector =
			List[(String, String)](("A", "C"), ("B", "D"), ("A", "B"))
		graph.incidenceVector(edgeVector) should be (List[Boolean](true, false, true))
	}

	@Test def testUnion() {
		val g1 = new Graph[String](
			Set[(String, String)](("A", "B"), ("B", "C")))
		val g2 =  new Graph[String](
			Set[(String, String)](("A", "B"), ("A", "C")))
		val u = new Graph[String](
			Set[(String, String)](("A", "B"), ("B", "C"), ("A", "C")))

		g1.union(g2) should be (u)
		g2.union(g1) should be (u)
	}

	@Test def testIntersection() {
		val g1 = new Graph[String](
			Set[(String, String)](("A", "B"), ("B", "C")))
		val g2 =  new Graph[String](
			Set[(String, String)](("A", "B"), ("A", "C")))
		val i = new Graph[String](
			Set[(String, String)](("A", "B")))

		g1.intersection(g2) should be (i)
		g2.intersection(g1) should be (i)
	}

	@Test def testEquals() {
		val g1 = new Graph[String](
			Set[(String, String)](("A", "B"), ("B", "C")))
		val g2 = new Graph[String](
			Set[(String, String)](("A", "B"), ("B", "C")))
		val g3 = new Graph[String](
			Set[(String, String)](("A", "B"), ("A", "C")))

		g1 should be (g2)
		g1.hashCode should be (g2.hashCode)
		g1 should not be (g3)
		assert(g1.canEqual(g3))
	}
}

class UndirectedGraphTest extends ShouldMatchersForJUnit {
	@Test def testEdgeSet() {
		val edges = Set[(String, String)](("A", "B"), ("B", "C"))
		new UndirectedGraph[String](edges).edgeSet should be (edges)
	}

	@Test def testEdgeSetOrdered() {
		val edges = Set[(String, String)](("A", "B"), ("B", "C"), ("B", "A"))
		new UndirectedGraph[String](edges).edgeSet should be (
			Set[(String, String)](("A", "B"), ("B", "C")))
	}

	@Test def testUndirectedNotEqualDirected() {
		val dir = new Graph[String](
			Set[(String, String)](("A", "B"), ("B", "C")))
		val udir = new UndirectedGraph[String](
			Set[(String, String)](("A", "B"), ("B", "C")))

		dir should not be (udir)
		udir should not be (dir)
	}

	@Test def testEquals() {
		val g1 = new UndirectedGraph[String](
			Set[(String, String)](("A", "B"), ("B", "C")))
		val g2 = new UndirectedGraph[String](
			Set[(String, String)](("A", "B"), ("C", "B")))
		val g3 = new UndirectedGraph[String](
			Set[(String, String)](("A", "B"), ("A", "C")))

		g1 should be (g2)
		g1.hashCode should be (g2.hashCode)
		g1 should not be (g3)
		assert(g1.canEqual(g3))
	}

	@Test def testUnionWithDirected() {
		val dir = new Graph[String](Set[(String, String)]())
		val udir = new UndirectedGraph[String](Set[(String, String)]())
		intercept[IllegalArgumentException] {
			dir.union(udir)
		}
		intercept[IllegalArgumentException] {
			udir.union(dir)
		}
	}

	@Test def testIntersectionWithDirected() {
		val dir = new Graph[String](Set[(String, String)]())
		val udir = new UndirectedGraph[String](Set[(String, String)]())
		intercept[IllegalArgumentException] {
			dir.intersection(udir)
		}
		intercept[IllegalArgumentException] {
			udir.intersection(dir)
		}
	}

	@Test def testIncidenceVector() {
		val graph = new UndirectedGraph[String](
			Set[(String, String)](("A", "B"), ("B", "C"), ("A", "C")))
		val edgeVector =
			List[(String, String)](("A", "C"), ("B", "D"), ("B", "A"))
		println(graph.incidenceVector(edgeVector))
		graph.incidenceVector(edgeVector) should be (List[Boolean](true, false, true))
	}

	@Test def testOrderEdge() {
		UndirectedGraph.order(("A", "B")) should be (("A", "B"))
		UndirectedGraph.order(("B", "A")) should be (("A", "B"))
	}
}
