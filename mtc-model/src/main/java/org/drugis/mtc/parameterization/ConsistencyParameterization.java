/*
 * This file is part of the GeMTC software for MTC model generation and
 * analysis. GeMTC is distributed from http://drugis.org/gemtc.
 * Copyright (C) 2009-2012 Gert van Valkenhoef.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Implements parameterization of consistency models for network meta-analysis.
 */
public class ConsistencyParameterization implements Parameterization {
	protected final Tree<Treatment, FoldedEdge<Treatment, Study>> d_tree;
	private final Map<Study, Treatment> d_baselines;
	
	/**
	 * Factory method to create a consistency parameterization for the given network.
	 */
	public static ConsistencyParameterization create(Network network) {
		Hypergraph<Treatment, Study> sGraph = NetworkModel.createStudyGraph(network);
		UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph = NetworkModel.createComparisonGraph(sGraph);
		Tree<Treatment, FoldedEdge<Treatment, Study>> tree = findSpanningTree(cGraph);
		Map<Study, Treatment> baselines = findStudyBaselines(sGraph, tree);
		ConsistencyParameterization pmtz = new ConsistencyParameterization(network, tree, baselines);
		return pmtz;
	}
	
	/**
	 * Find the minimum diameter spanning tree for the comparison graph.
	 * @param cGraph
	 * @return The minimum diameter spanning tree.
	 */
	public static Tree<Treatment, FoldedEdge<Treatment, Study>> findSpanningTree(UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph) {
		MinimumDiameterSpanningTree<Treatment, FoldedEdge<Treatment, Study>> mdst = new MinimumDiameterSpanningTree<Treatment, FoldedEdge<Treatment, Study>>(cGraph, TreatmentComparator.INSTANCE);
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

	protected static Treatment findMaxDegreeVertex(Tree<Treatment, FoldedEdge<Treatment, Study>> tree, Collection<Treatment> incidentVertices) {
		Iterator<Treatment> iterator = incidentVertices.iterator();
		Treatment maxDegree = iterator.next();
		while (iterator.hasNext()) {
			Treatment t = iterator.next();
			int degreeDiff = tree.getNeighborCount(t) - tree.getNeighborCount(maxDegree);
			if (degreeDiff > 0 || (degreeDiff == 0 && TreatmentComparator.INSTANCE.compare(t, maxDegree) < 0)) {
				maxDegree = t;
			}
		}
		return maxDegree;
	}

	/**
	 * Construct a consistency parameterization with the given spanning tree and study baselines.
	 * @param network Network to parameterize.
	 * @param tree Spanning tree that defines the basic parameters.
	 * @param baselines Map of studies to baseline treatments.
	 */
	public ConsistencyParameterization(Network network, Tree<Treatment, FoldedEdge<Treatment, Study>> tree, Map<Study, Treatment> baselines) {
		d_tree = tree;
		d_baselines = baselines;
	}

	// Documented in Parameterization
	public List<NetworkParameter> getParameters() {
		List<NetworkParameter> list = getBasicParameters(d_tree);
		Collections.sort(list, new ParameterComparator());
		return list;
	}

	/**
	 * Build an unordered list of basic parameters derived from the given tree.
	 */
	public static List<NetworkParameter> getBasicParameters(Tree<Treatment, FoldedEdge<Treatment, Study>> tree) {
		List<NetworkParameter> list = new ArrayList<NetworkParameter>();
		for (FoldedEdge<Treatment, Study> e : tree.getEdges()) {
			Treatment u = e.getVertices().getFirst();
			Treatment v = e.getVertices().getSecond();
			if (tree.findEdge(u, v) != null) {
				list.add(createBasic(u, v));
			} else {
				list.add(createBasic(v, u));
			}
		}
		return list;
	}

	// Documented in Parameterization
	public Map<NetworkParameter, Integer> parameterize(Treatment ta, Treatment tb) {
		// First try to find a basic parameter that corresponds to (ta, tb).
		if (d_tree.findEdge(ta, tb) != null) {
			return Collections.<NetworkParameter, Integer>singletonMap(createBasic(ta, tb), 1);
		}
		if (d_tree.findEdge(tb, ta) != null) {
			return Collections.<NetworkParameter, Integer>singletonMap(createBasic(tb, ta), -1);
		}
		// Now handle functional parameters.
		return parameterizeFunctional(ta, tb);
	}

	protected Map<NetworkParameter, Integer> parameterizeFunctional(Treatment ta, Treatment tb) {
		return pathToParameterization(GraphUtil.findPath(d_tree, ta, tb));
	}

	// Documented in Parameterization
	public Treatment getStudyBaseline(Study s) {
		return d_baselines.get(s);
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

	private static BasicParameter createBasic(Treatment first, Treatment second) {
		return new BasicParameter(first, second);
	}

	public List<List<Pair<Treatment>>> parameterizeStudy(Study s) {
		Treatment b = getStudyBaseline(s);
		List<Treatment> treatments = new ArrayList<Treatment>(s.getTreatments());
		treatments.remove(b);
		Collections.sort(treatments, TreatmentComparator.INSTANCE);
		
		List<Pair<Treatment>> list = new ArrayList<Pair<Treatment>>(s.getTreatments().size() - 1);
		for (Treatment t : treatments) {
			list.add(new Pair<Treatment>(b, t));
		}
		
		return Collections.singletonList(list);
	}

	public Graph<Treatment, FoldedEdge<Treatment, Study>> getBasicParameterTree() {
		return d_tree;
	}
}
