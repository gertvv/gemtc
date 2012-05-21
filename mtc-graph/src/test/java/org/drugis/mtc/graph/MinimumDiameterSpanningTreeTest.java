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
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import org.junit.Test;

import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class MinimumDiameterSpanningTreeTest {
	@Test
	public void testMinDiamTree() {
		Collection<String> vertices = Arrays.asList("A", "B", "C", "D", "E");
		final UndirectedGraph<String, Integer> g = new UndirectedSparseGraph<String, Integer>();
		GraphUtil.addVertices(g, vertices);
		g.addEdge(1, "A", "B");
		g.addEdge(2, "A", "C");
		g.addEdge(3, "A", "D");
		g.addEdge(4, "A", "E");
		g.addEdge(5, "B", "C");
		g.addEdge(6, "C", "D");
		g.addEdge(7, "D", "E");
		
		Tree<String, Integer> tree = new MinimumDiameterSpanningTree<String, Integer>(g).getMinimumDiameterSpanningTree();
		assertEquals("A", tree.getRoot());
		assertEquals(new Integer(1), tree.findEdge("A", "B"));
		assertEquals(new Integer(2), tree.findEdge("A", "C"));
		assertEquals(new Integer(3), tree.findEdge("A", "D"));
		assertEquals(new Integer(4), tree.findEdge("A", "E"));
		assertEquals(4, tree.getEdgeCount());
	}
	
	@Test
	public void testWithVertexComparator() {
		UndirectedGraph<String, Integer> graph = new UndirectedSparseGraph<String, Integer>();
		graph.addEdge(1, "F", "C");
		graph.addEdge(2, "B", "C");
		graph.addEdge(3, "B", "D");
		graph.addEdge(4, "C", "D");
		graph.addEdge(5, "A", "E");
		graph.addEdge(6, "A", "F");
		graph.addEdge(7, "E", "F");
		
		Tree<String, Integer> tree = new MinimumDiameterSpanningTree<String, Integer>(graph, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		}).getMinimumDiameterSpanningTree();
		assertEquals("C", tree.getRoot());
		assertNotNull(tree.findEdge("C", "F"));
		assertNotNull(tree.findEdge("C", "B"));
		assertNotNull(tree.findEdge("C", "D"));
		assertNotNull(tree.findEdge("F", "E"));
		assertNotNull(tree.findEdge("F", "A"));
	}
	
	@Test
	public void testWithLongShortestPaths() {
		UndirectedGraph<String, Integer> graph = new UndirectedSparseGraph<String, Integer>();
		graph.addEdge(1,  "CBGGEM", "CBPPTX");
		graph.addEdge(2,  "CBGGEM", "DDPGEM");
		graph.addEdge(3,  "CBGGEM", "DDPVNR");
		graph.addEdge(4,  "CBPPTX", "DDPDOC");
		graph.addEdge(5,  "CBPPTX", "DDPPTX");
		graph.addEdge(6,  "CBPPTX", "DDPETO");
		graph.addEdge(7,  "CBPPTX", "DDPGEM");
		graph.addEdge(8,  "CBPPTX", "DDPIRI");
		graph.addEdge(9,  "CBPPTX", "DDPVNR");
		graph.addEdge(10, "DDPGEM", "DDPPTX");
		graph.addEdge(11, "DDPGEM", "DDPPEM");
		graph.addEdge(12, "DDPGEM", "DDPVNR");
		graph.addEdge(13, "DDPGEM", "DDPIRI");
		graph.addEdge(14, "DDPDOC", "DDPVNR");
		graph.addEdge(15, "DDPDOC", "DDPPTX");
		graph.addEdge(16, "DDPDOC", "DDPGEM");
		graph.addEdge(17, "DDPPTX", "DDPVNR");
		graph.addEdge(18, "DDPETO", "DDPPTX");
		graph.addEdge(19, "DDPETO", "DDPGEM");
		graph.addEdge(20, "DDPIRI", "DDPVNR");
		graph.addEdge(21, "CBPDOC", "DDPVNR");
		graph.addEdge(22, "CBPPEM", "DDPPEM");
		
		new MinimumDiameterSpanningTree<String, Integer>(graph, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		}).getMinimumDiameterSpanningTree();
	}
}
