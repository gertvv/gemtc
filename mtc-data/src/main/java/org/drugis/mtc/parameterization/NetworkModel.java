package org.drugis.mtc.parameterization;

import org.drugis.mtc.graph.GraphUtil;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;

import edu.uci.ics.jung.algorithms.transformation.FoldingTransformerFixed;
import edu.uci.ics.jung.algorithms.transformation.FoldingTransformerFixed.FoldedEdge;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.SetHypergraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class NetworkModel {
	public static Hypergraph<Treatment, Study> createStudyGraph(Network network) {
		Hypergraph<Treatment, Study> graph = new SetHypergraph<Treatment, Study>();
		for (Treatment t : network.getTreatments()) {
			graph.addVertex(t);
		}
		for (Study s : network.getStudies()) {
			graph.addEdge(s, s.getTreatments());
		}
		return graph ;
	}
	
	public static UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> createComparisonGraph(Network network) {
		return createComparisonGraph(createStudyGraph(network));
	}

	public static UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> createComparisonGraph(Hypergraph<Treatment, Study> studyGraph) {
		return FoldingTransformerFixed.foldHypergraphEdges(studyGraph, UndirectedSparseGraph.<Treatment, FoldedEdge<Treatment, Study>>getFactory());
	}

	/**
	 * Convert the undirected comparison graph to a directed variant.
	 */
	public static DirectedGraph<Treatment, FoldedEdge<Treatment, Study>> toDirected(UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> ug) {
		DirectedGraph<Treatment, FoldedEdge<Treatment, Study>> dg = new DirectedSparseGraph<Treatment, FoldedEdge<Treatment,Study>>();
		GraphUtil.addVertices(dg, ug.getVertices());
		for (FoldedEdge<Treatment, Study> edge : ug.getEdges()) {
			Treatment t0 = edge.getVertices().getFirst();
			Treatment t1 = edge.getVertices().getSecond();
			dg.addEdge(edge, t0, t1);
			FoldedEdge<Treatment, Study> edge1 = new FoldedEdge<Treatment, Study>(t1, t0);
			edge1.getFolded().addAll(edge.getFolded());
			dg.addEdge(edge1, t1, t0);
		}
		return dg;
	}
}