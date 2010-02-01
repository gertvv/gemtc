package org.drugis.mtc

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

	@Test def testAdd() {
		val g1 = new Graph[String](
			Set[(String, String)](("A", "B"), ("B", "C")))
		val e = ("A", "C")
		val g2 = new Graph[String](
			Set[(String, String)](("A", "B"), ("B", "C"), ("A", "C")))

		g1.add(e) should be (g2)
	}

	@Test def testRemove() {
		val g1 = new Graph[String](
			Set[(String, String)](("A", "B"), ("B", "C")))
		val e = ("A", "C")
		val g2 = new Graph[String](
			Set[(String, String)](("A", "B"), ("B", "C"), ("A", "C")))

		g2.remove(e) should be (g1)
	}

	@Test def testRemoveSet() {
		val g1 = new Graph[String](
			Set[(String, String)](("B", "C")))
		val e = Set[(String, String)](("A", "C"), ("A", "B"))
		val g2 = new Graph[String](
			Set[(String, String)](("A", "B"), ("B", "C"), ("A", "C")))

		g2.remove(e) should be (g1)
	}

	@Test def testEdgesFrom() {
		val g = new Graph[String](
			Set[(String, String)](("A", "B"), ("B", "C"), ("A", "C")))
		val es = Set[(String, String)](("A", "B"), ("A", "C"))

		g.edgesFrom("A") should be (es)
	}

	@Test def testEdgesTo() {
		val g = new Graph[String](
			Set[(String, String)](("A", "B"), ("B", "C"), ("A", "C")))
		val es = Set[(String, String)](("A", "B"))

		g.edgesTo("B") should be (es)
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

	@Test def testAdd() {
		val g1 = new UndirectedGraph[String](
			Set[(String, String)](("A", "B"), ("B", "C")))
		val g2 = new UndirectedGraph[String](
			Set[(String, String)](("A", "B"), ("B", "C"), ("A", "C")))

		g1.add(("A", "C")) should be (g2)
		g1.add(("C", "A")) should be (g2)
	}

	@Test def testRemove() {
		val g1 = new UndirectedGraph[String](
			Set[(String, String)](("A", "B"), ("B", "C")))
		val g2 = new UndirectedGraph[String](
			Set[(String, String)](("A", "B"), ("B", "C"), ("A", "C")))

		g2.remove(("A", "C")) should be (g1)
		g2.remove(("C", "A")) should be (g1)
	}

	@Test def testRemoveSet() {
		val g1 = new UndirectedGraph[String](
			Set[(String, String)](("B", "C")))
		val g2 = new UndirectedGraph[String](
			Set[(String, String)](("A", "B"), ("B", "C"), ("A", "C")))

		g2.remove(Set(("A", "C"), ("B", "A"))) should be (g1)
		g2.remove(Set(("C", "A"), ("A", "B"))) should be (g1)
	}

	@Test def testEdgesFrom() {
		val g = new UndirectedGraph[String](
			Set[(String, String)](("A", "B"), ("B", "C"), ("A", "C")))
		val es = Set[(String, String)](("B", "A"), ("B", "C"))

		g.edgesFrom("B") should be (es)
	}

	@Test def testEdgesTo() {
		val g = new UndirectedGraph[String](
			Set[(String, String)](("A", "B"), ("B", "C"), ("A", "C")))
		val es = Set[(String, String)](("A", "B"), ("C", "B"))

		g.edgesTo("B") should be (es)
	}

	@Test def testFundamentalCycles() {
		val g = new UndirectedGraph[String](
			Set[(String, String)](
				("A", "B"), ("A", "C"), ("A", "D"),
				("B", "C"), ("B", "D"),
				("C", "D")))

		val t = new Tree[String](
			Set[(String, String)](
				("A", "B"), ("A", "C"), ("A", "D")), "A")
		
		val c1 = new UndirectedGraph[String](
			Set[(String, String)](
				("A", "B"), ("B", "C"), ("C", "A")))

		val c2 = new UndirectedGraph[String](
			Set[(String, String)](
				("A", "C"), ("C", "D"), ("D", "A")))

		val c3 = new UndirectedGraph[String](
			Set[(String, String)](
				("A", "B"), ("B", "D"), ("D", "A")))

		g.fundamentalCycles(t) should be (Set(c1, c2, c3))
	}
}

class TreeTest extends ShouldMatchersForJUnit {
	@Test def testPath() {
		val t = new Tree[String](Set[(String, String)](
			("A", "B"), ("B", "C"), ("A", "D")), "A")

		t.path("A", "C") should be (List[String]("A", "B", "C"))
		t.path("B", "D") should be (Nil)
	}

	@Test def testCommonAncestor() {
		val t = new Tree[String](Set[(String, String)](
			("A", "B"), ("B", "C"), ("B", "D"), ("A", "E")), "A")

		t.commonAncestor("C", "D") should be ("B")
		t.commonAncestor("B", "D") should be ("B")
		t.commonAncestor("E", "D") should be ("A")
	}

	@Test def testCycle() {
		val t = new Tree[String](Set[(String, String)](
			("A", "B"), ("B", "C"), ("B", "D"), ("A", "E")), "A")

		val c1 = List[String]("A", "B", "C", "E", "A")
		val c2 = List[String]("B", "C", "D", "B")

		t.cycle("C", "E") should be (c1)
		t.cycle("E", "C") should not be (c1)
		t.cycle("C", "D") should be (c2)
	}

	@Test def testCreateCycle() {
		val t = new Tree[String](Set[(String, String)](
			("A", "B"), ("B", "C"), ("B", "D"), ("A", "E")), "A")

		val c1 = new UndirectedGraph[String](Set[(String, String)](
			("A", "B"), ("B", "C"), ("C", "E"), ("E", "A")))

		val c2 = new UndirectedGraph[String](Set[(String, String)](
			("B", "C"), ("C", "D"), ("D", "B")))

		t.createCycle("E", "C") should be (c1)
		t.createCycle("C", "E") should be (c1)
		t.createCycle("C", "D") should be (c2)
		t.createCycle("C", "B").edgeSet.size should be (0)
	}
}

class FundamentalGraphBasisTest extends ShouldMatchersForJUnit {
	@Test
	def testConstruct() {
		val g = new UndirectedGraph[String](
			Set[(String, String)](
				("A", "B"), ("A", "C"), ("A", "D"),
				("B", "C"), ("B", "D"),
				("C", "D")))

		val t = new Tree[String](
			Set[(String, String)](
				("A", "B"), ("A", "C"), ("A", "D")), "A")

		val b = new FundamentalGraphBasis[String](g, t)

		val c1 = List[String]("A", "B", "C", "A")
		val c2 = List[String]("A", "C", "D", "A")
		val c3 = List[String]("A", "B", "D", "A")

		b.cycles should be (Set(c1, c2, c3))

		b.treeEdges should be (Set(("A", "B"), ("A", "C"), ("A", "D")))
		b.backEdges should be (Set(("B", "C"), ("B", "D"), ("C", "D")))
	}
}
