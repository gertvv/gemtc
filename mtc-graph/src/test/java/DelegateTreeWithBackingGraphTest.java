import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Tree;

public class DelegateTreeWithBackingGraphTest {
	@Test @Ignore
	public void testAddEdge() {
		DirectedGraph<String, Integer> graph = new DirectedSparseGraph<String, Integer>();
		graph.addVertex("A");
		graph.addEdge(1, "A", "B");
		
		Tree<String, Integer> tree = new DelegateTree<String, Integer>(graph);
		
		assertEquals(1, tree.getDepth("B"));
	}
}
