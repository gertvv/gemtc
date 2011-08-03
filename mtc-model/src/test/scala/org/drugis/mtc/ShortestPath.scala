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

class ShortestPathTest extends ShouldMatchersForJUnit {
	@Test def testSimpleGraphDistance() {
		val edges = Set[(String, String)](("A", "B"), ("B", "C"))
		val graph = new Graph[String](edges)
		val sp = ShortestPath.calculate(graph)
		sp.distance("A", "B") should be (Some(1))
		sp.distance("B", "A") should be (None)
	}

	@Test def testDerivedGraphDistance() {
		val edges = Set[(String, String)](("A", "B"), ("B", "C"))
		val graph = new Graph[String](edges)
		val sp = ShortestPath.calculate(graph)
		sp.distance("A", "C") should be (Some(2))
	}

	@Test def testUndirectedGraphDistance() {
		val edges = Set[(String, String)](("A", "B"), ("B", "C"), ("D", "E"))
		val graph = new UndirectedGraph[String](edges)
		val sp = ShortestPath.calculate(graph)
		sp.distance("A", "B") should be (Some(1))
		sp.distance("B", "A") should be (Some(1))
		sp.distance("B", "C") should be (Some(1))
		sp.distance("A", "C") should be (Some(2))
		sp.distance("C", "A") should be (Some(2))
		sp.distance("C", "D") should be (None)
	}

	@Test def testPath() {
		val edges = Set[(String, String)](("A", "B"), ("B", "C"))
		val graph = new Graph[String](edges)
		val sp = ShortestPath.calculate(graph)
		sp.path("A", "B") should be (Some(List("A", "B")))
		sp.path("A", "C") should be (Some(List("A", "B", "C")))
		sp.path("C", "A") should be (None)
	}
}

