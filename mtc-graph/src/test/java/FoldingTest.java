import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import edu.uci.ics.jung.algorithms.transformation.FoldingTransformerFixed;
import edu.uci.ics.jung.algorithms.transformation.FoldingTransformerFixed.FoldedEdge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.SetHypergraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;


public class FoldingTest {
	@Test 
	public void testFoldingBreaksEdgeAccess() {
		Hypergraph<String, Integer> hGraph = new SetHypergraph<String, Integer>();
		hGraph.addVertex("A");
		hGraph.addVertex("B");
		hGraph.addVertex("C");
		hGraph.addVertex("D");
		hGraph.addEdge(1, Arrays.asList("A", "B"));
		hGraph.addEdge(2, Arrays.asList("B", "A"));
		hGraph.addEdge(3, Arrays.asList("A", "B", "C"));
		hGraph.addEdge(4, Arrays.asList("B", "C"));
		
		// This is kind of awkward... if the method accepted a Factory<UndirectedGraph> one could
		// just use UndirectedSparseGraph.getFactory()
		Graph<String, FoldedEdge<String, Integer>> graph = FoldingTransformerFixed.foldHypergraphEdges(hGraph,
				UndirectedSparseGraph.<String, FoldedEdge<String, Integer>>getFactory());
		
		// The vertices are copied fine
		assertEquals(new HashSet<String>(Arrays.asList("A", "B", "C", "D")), new HashSet<String>(graph.getVertices()));
		
		// The following works file: getting the edges for each of the vertices
		assertEquals(3, graph.getEdgeCount());
		assertEquals(new HashSet<Integer>(Arrays.asList(1, 2, 3)), new HashSet<Integer>(graph.findEdge("A", "B").getFolded()));
		assertEquals(new HashSet<Integer>(Arrays.asList(3)), new HashSet<Integer>(graph.findEdge("A", "C").getFolded()));
		assertEquals(new HashSet<Integer>(Arrays.asList(3, 4)), new HashSet<Integer>(graph.findEdge("B", "C").getFolded()));

		// But this does not work: java.lang.AssertionError: expected:<<A, B>> but was:<null>
		assertEquals(new Pair<String>("A", "B"), graph.getEndpoints(graph.findEdge("A", "B")));
	}
}
