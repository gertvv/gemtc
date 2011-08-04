/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
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

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

class AbsoluteOneCenterTest extends ShouldMatchersForJUnit {
	@Test def testIntersect() {
		val dm = List(
			List(0.0, 1.0, 2.0, 3.0, 4.0),
			List(1.0, 0.0, 1.0, 2.0, 3.0),
			List(2.0, 1.0, 0.0, 1.0, 2.0),
			List(3.0, 2.0, 1.0, 0.0, 1.0),
			List(4.0, 3.0, 2.0, 1.0, 0.0)
			)
		AbsoluteOneCenter.intersect(dm, (1, 2), 0, 3) should be (Some(0.5))
		AbsoluteOneCenter.intersect(dm, (1, 2), 0, 4) should be (None)
		val dm2 = List(
			List(0.0, 1.0, 2.0, 3.0, 3.5),
			List(1.0, 0.0, 1.0, 2.0, 2.5),
			List(2.0, 1.0, 0.0, 1.0, 1.5),
			List(3.0, 2.0, 1.0, 0.0, 0.5),
			List(3.5, 2.5, 1.5, 0.5, 0.0)
			)
		AbsoluteOneCenter.intersect(dm2, (1, 2), 0, 3) should be (Some(0.5))
		AbsoluteOneCenter.intersect(dm2, (1, 2), 0, 4) should be (Some(0.75))
	}

	@Test def testVertexOrder() {
		val dm = List(
			List(0.0, 1.0, 2.0, 3.0, 4.0),
			List(1.0, 0.0, 1.0, 2.0, 3.0),
			List(2.0, 1.0, 0.0, 1.0, 2.0),
			List(3.0, 2.0, 1.0, 0.0, 1.0),
			List(4.0, 3.0, 2.0, 1.0, 0.0)
			)
		val l = AbsoluteOneCenter.distanceOrderedVertices(dm)
		l(0) should be (List(4, 3, 2, 1, 0))
		l(1) should be (List(4, 3, 0, 2, 1))
		l(2) should be (List(0, 4, 1, 3, 2))
	}

	@Test def testLocalCenter() {
		val dm = List(
			List(0.0, 1.0, 2.0, 3.0, 4.0),
			List(1.0, 0.0, 1.0, 2.0, 3.0),
			List(2.0, 1.0, 0.0, 1.0, 2.0),
			List(3.0, 2.0, 1.0, 0.0, 1.0),
			List(4.0, 3.0, 2.0, 1.0, 0.0)
			)
		val l = AbsoluteOneCenter.distanceOrderedVertices(dm)
		AbsoluteOneCenter.localCenter((1, 2), dm, l) should be ((1.0, 2.0))
		AbsoluteOneCenter.localCenter((0, 1), dm, l) should be ((1.0, 3.0))
		AbsoluteOneCenter.localCenter((2, 3), dm, l) should be ((0.0, 2.0))
		val dm2 = List(
			List(0.0, 1.0, 2.0, 3.0, 3.5),
			List(1.0, 0.0, 1.0, 2.0, 2.5),
			List(2.0, 1.0, 0.0, 1.0, 1.5),
			List(3.0, 2.0, 1.0, 0.0, 0.5),
			List(3.5, 2.5, 1.5, 0.5, 0.0)
			)
		AbsoluteOneCenter.localCenter((1, 2), dm2, l) should be ((0.75, 1.75))
		AbsoluteOneCenter.localCenter((2, 3), dm2, l) should be ((0.0, 2.0))
	}

	@Test def testSimpleVertexCenter() {
		val edges = Set[(String, String)](("A", "B"), ("B", "C"), ("B", "D"))
		val graph = new UndirectedGraph[String](edges)
		val sp = ShortestPath.calculate(graph)
		AbsoluteOneCenter(graph, sp) should be (PointCenter("A", "B", 1.0))

	}

	@Test def testSimplePointCenter() {
		val edges = Set[(String, String)](("A", "B"), ("B", "C"), ("C", "D"))
		val graph = new UndirectedGraph[String](edges)
		val sp = ShortestPath.calculate(graph)
		AbsoluteOneCenter(graph, sp) should be (PointCenter("B", "C", 0.5))
	}

	@Test def testCenterWithCycles() {
		val edges = Set[(String, String)](("A", "B"), ("A", "C"), ("A", "D"), ("A", "E"), ("B", "C"), ("C", "D"), ("D", "E"))
		val graph = new UndirectedGraph[String](edges)
		val sp = ShortestPath.calculate(graph)
		AbsoluteOneCenter(graph, sp) should be (PointCenter("A", "B", 0.0))

	}
}

class MinimumDiameterSpanningTreeTest extends ShouldMatchersForJUnit {
	@Test def testShortestPath() {
		val edges = Set[(String, String)](("A", "B"), ("B", "C"), ("C", "D"))
		val graph = new UndirectedGraph[String](edges)
		val sp = ShortestPath.calculate(graph)
		MinimumDiameterSpanningTree.shortestPath(sp, PointCenter("B", "C", 0.0), "D") should be (List("B", "C", "D"))
		MinimumDiameterSpanningTree.shortestPath(sp, PointCenter("B", "C", 0.1), "D") should be (List("C", "D"))
		MinimumDiameterSpanningTree.shortestPath(sp, PointCenter("B", "C", 0.9), "A") should be (List("B", "A"))
		MinimumDiameterSpanningTree.shortestPath(sp, PointCenter("B", "C", 1.0), "A") should be (List("B", "A"))
	}
}
