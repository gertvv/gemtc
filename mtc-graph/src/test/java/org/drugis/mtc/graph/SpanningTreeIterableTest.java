/*
 * This file is part of the GeMTC software for MTC model generation and
 * analysis. GeMTC is distributed from http://drugis.org/gemtc.
 * Copyright (C) 2009-2012 Gert van Valkenhoef.
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

package org.drugis.mtc.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.collections15.Factory;
import org.junit.Test;

import edu.uci.ics.jung.algorithms.transformation.DirectionTransformer;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class SpanningTreeIterableTest {
	@Test
	public void testEnumerateDirected() {
		DirectedGraph<String, Integer> g = new DirectedSparseGraph<String, Integer>();
		GraphUtil.addVertices(g, Arrays.asList("A", "B", "C", "D"));
		g.addEdge(1, "A", "B");
		g.addEdge(2, "A", "C");
		g.addEdge(3, "B", "D");
		g.addEdge(4, "D", "C");
		g.addEdge(5, "B", "C");
		g.addEdge(6, "C", "B");

		Iterator<Tree<String, Integer>> iterator = new SpanningTreeIterable<String, Integer>(g, "A").iterator(); 

		assertTrue(iterator.hasNext());
		Tree<String, Integer> tree1 = iterator.next();
		assertTrue(tree1.containsEdge(1));
		assertTrue(tree1.containsEdge(3));
		assertTrue(tree1.containsEdge(4));
		assertEquals(3, tree1.getEdgeCount());
		//("A", "B"), ("B", "D"), ("D", "C")

		assertTrue(iterator.hasNext());
		Tree<String, Integer> tree2 = iterator.next();
		assertTrue(tree2.containsEdge(1));
		assertTrue(tree2.containsEdge(3));
		assertTrue(tree2.containsEdge(5));
		assertEquals(3, tree2.getEdgeCount());
		//("A", "B"), ("B", "D"), ("B", "C")

		assertTrue(iterator.hasNext());
		Tree<String, Integer> tree3 = iterator.next();
		assertTrue(tree3.containsEdge(1));
		assertTrue(tree3.containsEdge(3));
		assertTrue(tree3.containsEdge(2));
		assertEquals(3, tree3.getEdgeCount());
		//("A", "B"), ("B", "D"), ("A", "C")

		assertTrue(iterator.hasNext());
		Tree<String, Integer> tree4 = iterator.next();
		assertTrue(tree4.containsEdge(2));
		assertTrue(tree4.containsEdge(6));
		assertTrue(tree4.containsEdge(3));
		assertEquals(3, tree4.getEdgeCount());
		//("A", "C"), ("C", "B"), ("B", "D")

		assertFalse(iterator.hasNext());
	}

	@Test
	public void testEnumerateUndirected() {
		UndirectedGraph<String, Integer> ug = new UndirectedSparseGraph<String, Integer>();
		GraphUtil.addVertices(ug, Arrays.asList("A", "B", "C", "D"));
		ug.addEdge(1, "A", "B");
		ug.addEdge(2, "A", "C");
		ug.addEdge(3, "A", "D");
		ug.addEdge(4, "B", "C");
		ug.addEdge(5, "B", "D");
		ug.addEdge(6, "C", "D");
		
		DirectedGraph<String, Integer> dg = (DirectedGraph<String, Integer>) DirectionTransformer.toDirected(ug, DirectedSparseGraph.<String, Integer>getFactory(), new Factory<Integer>() {
			int cnt = 0;
			public Integer create() {
				return ++cnt;
			}
		}, true);

		Iterator<Tree<String, Integer>> iterator = new SpanningTreeIterable<String, Integer>(dg, "A").iterator();
		int i;
		for (i = 0; iterator.hasNext(); ++i) {
			iterator.next();
		}
		assertEquals(16, i);
	}
}
