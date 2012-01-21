package org.drugis.mtc.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

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
	
	@Test
	public void testIsWeaklyConnected() {
		Graph<String, Integer> graph = new SparseMultigraph<String, Integer>();
		graph.addEdge(1, "A", "B", EdgeType.DIRECTED);
		graph.addVertex("C");
		
		assertFalse(GraphUtil.isWeaklyConnected(graph));
		
		graph.addEdge(2, "A", "C");
		assertTrue(GraphUtil.isWeaklyConnected(graph));
		
		graph.addEdge(3, "D", "E");
		assertFalse(GraphUtil.isWeaklyConnected(graph));
	}
	
	@Test
	public void testFindCommonAncestor() {
		Tree<String, Integer> tree = new DelegateTree<String, Integer>();
		tree.addVertex("A");
		tree.addEdge(1, "A", "B");
		tree.addEdge(2, "B", "C");
		tree.addEdge(3, "B", "D");
		tree.addEdge(4, "A", "E");
		
		assertEquals("D", GraphUtil.findCommonAncestor(tree, "D", "D"));
		assertEquals("B", GraphUtil.findCommonAncestor(tree, "C", "D"));
		assertEquals("B", GraphUtil.findCommonAncestor(tree, "B", "D"));
		assertEquals("A", GraphUtil.findCommonAncestor(tree, "E", "D"));
	}
	
	@Test
	public void findPath() {
		Tree<String, Integer> tree = new DelegateTree<String, Integer>();
		tree.addVertex("A");
		tree.addEdge(1, "A", "B");
		tree.addEdge(2, "B", "C");
		tree.addEdge(3, "B", "D");
		tree.addEdge(4, "A", "E");
		
		assertEquals(Arrays.asList("D"), GraphUtil.findPath(tree, "D", "D"));
		assertEquals(Arrays.asList("C", "B", "D"), GraphUtil.findPath(tree, "C", "D"));
		assertEquals(Arrays.asList("B", "D"), GraphUtil.findPath(tree, "B", "D"));
		assertEquals(Arrays.asList("E", "A", "B", "D"), GraphUtil.findPath(tree, "E", "D"));		
	}
	/*
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
}*/

}
