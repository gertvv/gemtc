package org.drugis.mtc.graph;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class MinimumDiameterSpanningTreeTest {
	@Test
	public void testMinDiamTree() {
		final UndirectedGraph<String, Integer> g = new UndirectedSparseGraph<String, Integer>();
		g.addVertex("A");
		g.addVertex("B");
		g.addVertex("C");
		g.addVertex("D");
		g.addVertex("E");
		g.addEdge(1, "A", "B");
		g.addEdge(2, "A", "C");
		g.addEdge(3, "A", "D");
		g.addEdge(4, "A", "E");
		g.addEdge(5, "B", "C");
		g.addEdge(6, "C", "D");
		g.addEdge(7, "D", "E");

		Tree<String, Integer> tree = new MinimumDiameterSpanningTree<String, Integer>(g).getMinimumDiameterSpanningTree();
		assertEquals("A", tree.getRoot());
		assertNotNull(tree.findEdge("A", "B"));
		assertNotNull(tree.findEdge("A", "C"));
		assertNotNull(tree.findEdge("A", "D"));
		assertNotNull(tree.findEdge("A", "E"));
		assertEquals(4, tree.getEdgeCount());
	}

}
