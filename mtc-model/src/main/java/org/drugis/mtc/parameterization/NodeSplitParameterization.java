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
import java.util.HashSet;
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
	public static List<BasicParameter> getSplittableNodes(Network network) {
		Hypergraph<Treatment, Study> studyGraph = NetworkModel.createStudyGraph(network);
		UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph = NetworkModel.createComparisonGraph(studyGraph);
		return getSplittableNodes(studyGraph, cGraph);
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
		return GraphUtil.areVerticesWeaklyConnected(reduced, split.getFirst(), split.getSecond());
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
		parameters.add(getDirectParameter());
		return parameters;
	}
	
	@Override
	public Map<NetworkParameter, Integer> parameterize(Treatment ta, Treatment tb) {
		if (d_splitNode.getBaseline().equals(ta) && d_splitNode.getSubject().equals(tb)) {
			Map<NetworkParameter, Integer> map = new HashMap<NetworkParameter, Integer>();
			map.put(getDirectParameter(), 1);
			return map;
		}
		if (d_splitNode.getBaseline().equals(tb) && d_splitNode.getSubject().equals(ta)) {
			Map<NetworkParameter, Integer> map = new HashMap<NetworkParameter, Integer>();
			map.put(getDirectParameter(), -1);
			return map;			
		}
		return super.parameterize(ta, tb);
	}
	
	public SplitParameter getDirectParameter() {
		return new SplitParameter(d_splitNode.getBaseline(), d_splitNode.getSubject(), true);
	}
	
	public SplitParameter getIndirectParameter() {
		return new SplitParameter(d_splitNode.getBaseline(), d_splitNode.getSubject(), false);
	}
	
	public Map<NetworkParameter, Integer> parameterizeIndirect() {
		return super.parameterize(d_splitNode.getBaseline(), d_splitNode.getSubject());
	}
	
	@Override
	public List<List<Pair<Treatment>>> parameterizeStudy(Study s) {
		Pair<Treatment> split = new Pair<Treatment>(d_splitNode.getBaseline(), d_splitNode.getSubject());
		if (s.getTreatments().size() == 2 || !s.getTreatments().containsAll(split)) {
			return super.parameterizeStudy(s);
		}
		List<Pair<Treatment>> params1 = super.parameterizeStudy(s).get(0);
		params1.remove(new Pair<Treatment>(getStudyBaseline(s), d_splitNode.getSubject()));
		List<Pair<Treatment>> params2 = new ArrayList<Pair<Treatment>>();
		params2.add(split);
		List<List<Pair<Treatment>>> params = new ArrayList<List<Pair<Treatment>>>();
		params.add(params1);
		params.add(params2);
		return params;
	}

	public BasicParameter getSplitNode() {
		return d_splitNode;
	}
}
