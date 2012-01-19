package org.drugis.mtc.graph;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

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

}
