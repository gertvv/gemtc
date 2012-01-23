package org.drugis.mtc.parameterization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drugis.mtc.graph.GraphUtil;
import org.drugis.mtc.graph.MinimumDiameterSpanningTree;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;

import edu.uci.ics.jung.algorithms.transformation.FoldingTransformerFixed.FoldedEdge;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.UndirectedGraph;

public class ConsistencyParameterization implements Parameterization {
	private final Tree<Treatment, FoldedEdge<Treatment, Study>> d_tree;

	/**
	 * Find the minimum diameter spanning tree for the comparison graph.
	 * @param cGraph
	 * @return The minimum diameter spanning tree.
	 */
	public static Tree<Treatment, FoldedEdge<Treatment, Study>> findSpanningTree(UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph) {
		MinimumDiameterSpanningTree<Treatment, FoldedEdge<Treatment, Study>> mdst = new MinimumDiameterSpanningTree<Treatment, FoldedEdge<Treatment, Study>>(cGraph, new TreatmentComparator());
		return mdst.getMinimumDiameterSpanningTree();
	}
	
	/**
	 * Find the study baseline assignment that maximizes the degree of each baseline in the given spanning tree.
	 * @param studyGraph The study graph.
	 * @param tree The spanning tree
	 */
	public static Map<Study, Treatment> findStudyBaselines(Hypergraph<Treatment, Study> studyGraph, Tree<Treatment, FoldedEdge<Treatment, Study>> tree) {
		Map<Study, Treatment> map = new HashMap<Study, Treatment>();
		for (Study s : studyGraph.getEdges()) {
			map.put(s, findMaxDegreeVertex(tree, studyGraph.getIncidentVertices(s)));
		}
		return map;
	}
	
	private static TreatmentComparator s_tc = new TreatmentComparator();

	private static Treatment findMaxDegreeVertex(Tree<Treatment, FoldedEdge<Treatment, Study>> tree, Collection<Treatment> incidentVertices) {
		Iterator<Treatment> iterator = incidentVertices.iterator();
		Treatment maxDegree = iterator.next();
		while (iterator.hasNext()) {
			Treatment t = iterator.next();
			int degreeDiff = tree.getNeighborCount(t) - tree.getNeighborCount(maxDegree);
			if (degreeDiff > 0 || (degreeDiff == 0 && s_tc.compare(t, maxDegree) < 0)) {
				maxDegree = t;
			}
		}
		return maxDegree;
	}

	public ConsistencyParameterization(Network network, Tree<Treatment, FoldedEdge<Treatment, Study>> tree) {
		d_tree = tree;
	}

	public List<NetworkParameter> getParameters() {
		ArrayList<NetworkParameter> list = new ArrayList<NetworkParameter>();
		for (FoldedEdge<Treatment, Study> e : d_tree.getEdges()) {
			list.add(createBasic(e));
		}
		Collections.sort(list, new NetworkParameterComparator());
		return list;
	}

	private BasicParameter createBasic(FoldedEdge<Treatment, Study> e) {
		Treatment first = e.getVertices().getFirst();
		Treatment second = e.getVertices().getSecond();
		return createBasic(first, second);
	}

	private BasicParameter createBasic(Treatment first, Treatment second) {
		return new BasicParameter(first, second);
	}

	public Map<NetworkParameter, Integer> parameterize(Treatment ta, Treatment tb) {
		// First try to find a basic parameter that corresponds to (ta, tb).
		if (d_tree.findEdge(ta, tb) != null) {
			return Collections.<NetworkParameter, Integer>singletonMap(createBasic(ta, tb), 1);
		}
		if (d_tree.findEdge(tb, ta) != null) {
			return Collections.<NetworkParameter, Integer>singletonMap(createBasic(tb, ta), -1);
		}
		// Now handle functional parameters.
		return pathToParameterization(GraphUtil.findPath(d_tree, ta, tb));
	}

	private Map<NetworkParameter, Integer> pathToParameterization(List<Treatment> path) {
		Map<NetworkParameter, Integer> map = new HashMap<NetworkParameter, Integer>();
		for (int i = 1; i < path.size(); ++i) {
			Treatment u = path.get(i - 1);
			Treatment v = path.get(i);
			if (d_tree.findEdge(u, v) != null) {
				map.put(createBasic(u, v), 1);
			} else {
				map.put(createBasic(v, u), -1);
			}
		}
		return map;
	}

	public Treatment getStudyBaseline(Study s) {
		// TODO Auto-generated method stub
		return null;
	}
}
