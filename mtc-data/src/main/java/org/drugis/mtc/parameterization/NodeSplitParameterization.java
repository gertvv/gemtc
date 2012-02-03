package org.drugis.mtc.parameterization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drugis.mtc.graph.GraphUtil;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;

import edu.uci.ics.jung.algorithms.shortestpath.PrimMinimumSpanningTree;
import edu.uci.ics.jung.algorithms.transformation.FoldingTransformerFixed.FoldedEdge;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.SetHypergraph;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Implements parameterization of node-splitting models for network meta-analysis.
 * WARNING: this method is a work-in-progress, correctness is TBD.
 */
public class NodeSplitParameterization extends ConsistencyParameterization {
	private final BasicParameter d_splitNode;

	/**
	 * Factory method to create a node-split parameterization for the given network and split node.
	 */
	public static NodeSplitParameterization create(Network network, BasicParameter split) {
		Hypergraph<Treatment, Study> sGraph = NetworkModel.createStudyGraph(network);
		UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph = NetworkModel.createComparisonGraph(sGraph);
		Tree<Treatment, FoldedEdge<Treatment, Study>> tree = findSpanningTree(cGraph, split);
		Map<Study, Treatment> baselines = findStudyBaselines(sGraph, tree, split);
		NodeSplitParameterization pmtz = new NodeSplitParameterization(network, split, tree, baselines);
		return pmtz;
	}
	
	/**
	 * Determine which nodes should be split.
	 * @see isSplittable
	 */
	public static List<BasicParameter> getSplittableNodes(Hypergraph<Treatment, Study> studyGraph, UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph) {
		// Get any spanning tree
		PrimMinimumSpanningTree<Treatment, FoldedEdge<Treatment, Study>> treeBuilder = 
			new PrimMinimumSpanningTree<Treatment, FoldedEdge<Treatment,Study>>(DelegateTree.<Treatment, FoldedEdge<Treatment,Study>>getFactory());
		Tree<Treatment, FoldedEdge<Treatment,Study>> tree = (Tree<Treatment, FoldedEdge<Treatment, Study>>) treeBuilder.transform(cGraph);

		// The spanning tree gives us a cycle basis, to determine which comparisons occur in a cycle
		// FIXME: move to GraphUtil
		Set<FoldedEdge<Treatment,Study>> nonTreeEdges = new HashSet<FoldedEdge<Treatment,Study>>(cGraph.getEdges());
		nonTreeEdges.removeAll(tree.getEdges());
		Set<BasicParameter> inCycle = new HashSet<BasicParameter>();
		for (FoldedEdge<Treatment, Study> e : nonTreeEdges) {
			List<Treatment> cycle = GraphUtil.findPath(tree, e.getVertices().getFirst(), e.getVertices().getSecond());
			cycle.add(e.getVertices().getFirst());
			for (int i = 1; i < cycle.size(); ++i) {
				Pair<Treatment> treatments = cGraph.findEdge(cycle.get(i - 1), cycle.get(i)).getVertices();
				inCycle.add(new BasicParameter(treatments.getFirst(), treatments.getSecond()));
			}
		}
		
		// Now, determine which of the comparisons satisfy the rule
		List<BasicParameter> paramList = new ArrayList<BasicParameter>();
		for (BasicParameter param : inCycle) {
			if (isSplittable(studyGraph, new Pair<Treatment>(param.getBaseline(), param.getSubject()))) {
				paramList.add(param);
			}
		}
		
		Collections.sort(paramList);
		return paramList;
	}
	
	/**
	 * Applies the rule 
	 * "for a given set of studies S, split {x,y} if and only if there is a path between x and y
	 * in the reduced network determined by removing the x and y arms from all studies
	 * that include both x and y."
	 */
	public static boolean isSplittable(Hypergraph<Treatment, Study> studyGraph, Pair<Treatment> split) {
		SetHypergraph<Treatment, Study> reduced = new SetHypergraph<Treatment, Study>();
		for (Treatment t : studyGraph.getVertices()) {
			reduced.addVertex(t);
		}
		for (Study s : studyGraph.getEdges()) {
			Collection<Treatment> incidentVertices = studyGraph.getIncidentVertices(s);
			if (incidentVertices.containsAll(split)) {
				if (incidentVertices.size() > 3) {
					Set<Treatment> reducedVertices = new HashSet<Treatment>(studyGraph.getVertices());
					reducedVertices.removeAll(split);
					reduced.addEdge(s, reducedVertices);
				}
			} else {
				reduced.addEdge(s, incidentVertices);
			}
		}
		return areVerticesConnected(reduced, split.getFirst(), split.getSecond());
	}
	
	// FIXME: move to GraphUtil
	private static <V,E> boolean areVerticesConnected(Hypergraph<V, E> studyGraph, V first, V second) {
		Set<V> visited = new HashSet<V>();
		LinkedList<V> fringe = new LinkedList<V>();
		fringe.add(first);
		while (!fringe.isEmpty()) {
			V v = fringe.pop();
			if (v.equals(second)) {
				return true;
			}
			visited.add(v);
			HashSet<V> neighbors = new HashSet<V>(studyGraph.getNeighbors(v));
			neighbors.removeAll(visited);
			fringe.addAll(neighbors);
		}
		return false;
	}

	public static Tree<Treatment, FoldedEdge<Treatment, Study>> findSpanningTree(UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph, BasicParameter split) {
		UndirectedSparseGraph<Treatment, FoldedEdge<Treatment, Study>> graph = new UndirectedSparseGraph<Treatment, FoldedEdge<Treatment, Study>>();
		GraphUtil.copyGraph(cGraph, graph);
		graph.removeEdge(cGraph.findEdge(split.getBaseline(), split.getSubject()));
		return ConsistencyParameterization.findSpanningTree(graph);
	}
	
	/**
	 * Find the study baseline assignment that maximizes the degree of each baseline in the given spanning tree,
	 * but exclude the split node if it occurs in that study.
	 * @param studyGraph The study graph.
	 * @param tree The spanning tree
	 */
	public static Map<Study, Treatment> findStudyBaselines(Hypergraph<Treatment, Study> studyGraph, Tree<Treatment, FoldedEdge<Treatment, Study>> tree, BasicParameter split) {
		Pair<Treatment> splitVertices = new Pair<Treatment>(split.getBaseline(), split.getSubject());
		Map<Study, Treatment> map = new HashMap<Study, Treatment>();
		for (Study s : studyGraph.getEdges()) {
			Collection<Treatment> incidentVertices = studyGraph.getIncidentVertices(s);
			if (incidentVertices.size() > 2 && incidentVertices.containsAll(splitVertices)) { // taboo the split vertices in studies that contain both
				incidentVertices = new HashSet<Treatment>(incidentVertices);
				incidentVertices.removeAll(splitVertices);
			}
			map.put(s, findMaxDegreeVertex(tree, incidentVertices));
		}
		return map;
	}

	public NodeSplitParameterization(Network network, BasicParameter splitNode, Tree<Treatment, FoldedEdge<Treatment, Study>> tree, Map<Study, Treatment> baselines) {
		super(network, tree, baselines);
		d_splitNode = splitNode;
	}

	@Override
	public List<NetworkParameter> getParameters() {
		List<NetworkParameter> parameters = super.getParameters();
		parameters.add(new SplitParameter(d_splitNode.getBaseline(), d_splitNode.getSubject(), true));
		return parameters;
	}
	// FIXME: need a new concept of how to parameterize the individual studies -- this cannot be the same as for consistency models!
}
