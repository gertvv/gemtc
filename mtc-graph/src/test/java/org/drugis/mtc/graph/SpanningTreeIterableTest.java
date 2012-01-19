package org.drugis.mtc.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Tree;

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
//
//		@Test def testEnumerateUndirected() {
//			val g = new UndirectedGraph[String](Set[(String, String)](
//					("A", "B"), ("A", "C"), ("A", "D"),
//					("B", "C"), ("B", "D"),
//					("C", "D")
//				))
//
//			val found = SpanningTreeEnumerator.treeEnumerator(g, "A").toList
//			(Set[Tree[String]]() ++ found).size should be (found.size)
//			found.size should be (16)
//		}
}
