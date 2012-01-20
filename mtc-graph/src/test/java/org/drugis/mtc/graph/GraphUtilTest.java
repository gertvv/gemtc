package org.drugis.mtc.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

public class GraphUtilTest {
	@Test
	public void testCopyGraph() {
		Graph<String, Integer> graph = new SparseMultigraph<String, Integer>();
		graph.addEdge(1, "A", "B", EdgeType.DIRECTED);
		graph.addEdge(2, "A", "B", EdgeType.DIRECTED);
		graph.addEdge(4, "D", "E", EdgeType.UNDIRECTED);
		graph.addEdge(5, "E", "D", EdgeType.DIRECTED);
		graph.addVertex("C");
		
		Graph<String, Integer> copy = new SparseMultigraph<String, Integer>();
		GraphUtil.copyGraph(graph, copy);
		
		assertEquals(graph.getVertexCount(), copy.getVertexCount());
		assertEquals(graph.getEdgeCount(), copy.getEdgeCount());
		assertTrue(copy.containsVertex("A"));
		assertTrue(copy.containsVertex("B"));
		assertTrue(copy.containsVertex("C"));
		assertTrue(copy.containsVertex("D"));
		assertTrue(copy.containsVertex("E"));
		assertTrue(copy.containsEdge(1));
		assertTrue(copy.containsEdge(2));
		assertTrue(copy.containsEdge(4));
		assertTrue(copy.containsEdge(5));
		assertEquals(EdgeType.DIRECTED, copy.getEdgeType(1));
		assertEquals(EdgeType.DIRECTED, copy.getEdgeType(2));
		assertEquals(EdgeType.UNDIRECTED, copy.getEdgeType(4));
		assertEquals(EdgeType.DIRECTED, copy.getEdgeType(5));
		assertEquals(new Pair<String>("A", "B"), copy.getEndpoints(1));
		assertEquals(new Pair<String>("A", "B"), copy.getEndpoints(2));
		assertEquals(new Pair<String>("D", "E"), copy.getEndpoints(4));
		assertEquals(new Pair<String>("E", "D"), copy.getEndpoints(5));
	}
	
	@Test
	public void testCopyTree() {
		DelegateTree<String, Integer> tree = new DelegateTree<String, Integer>();
		tree.setRoot("A");
		tree.addChild(1, "A", "D");
		tree.addChild(2, "D", "B");
		tree.addChild(3, "D", "C");
		tree.addChild(4, "D", "E");
		tree.addChild(5, "A", "F");
		tree.addChild(6, "C", "G");
		
		Tree<String, Integer> copy = new DelegateTree<String, Integer>();
		GraphUtil.copyTree(tree, copy);
		assertEquals(tree.getVertexCount(), copy.getVertexCount());
		assertEquals(tree.getEdgeCount(), copy.getEdgeCount());
	}
}
