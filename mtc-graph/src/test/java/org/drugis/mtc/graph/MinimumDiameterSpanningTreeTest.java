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

}
