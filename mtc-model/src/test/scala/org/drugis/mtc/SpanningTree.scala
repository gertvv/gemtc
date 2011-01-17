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

class SpanningTreeRequiredEdgeTest extends ShouldMatchersForJUnit {
	@Test def testEnumerateUndirected() {
		val g = new UndirectedGraph[String](Set[(String, String)](
				("A", "B"), ("A", "C"), ("B", "D"), ("C", "D")
			))

		val found = SpanningTreeEnumerator.treeEnumerator(g, "A", "C")
		(Set[Tree[String]]() ++ found) should be (
			Set[Tree[String]](
				new Tree(Set(("A", "C"), ("C", "D"), ("D", "B")), "A"),
				new Tree(Set(("A", "B"), ("A", "C"), ("C", "D")), "A"),
				new Tree(Set(("A", "B"), ("B", "D"), ("A", "C")), "A")
			)
		)
	}
}
