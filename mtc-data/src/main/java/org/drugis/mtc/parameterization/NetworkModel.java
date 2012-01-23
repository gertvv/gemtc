package org.drugis.mtc.parameterization;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;

import edu.uci.ics.jung.algorithms.transformation.FoldingTransformerFixed;
import edu.uci.ics.jung.algorithms.transformation.FoldingTransformerFixed.FoldedEdge;
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
			graph.addEdge(s, getTreatments(s));
		}
		return graph ;
	}
	
	public static UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> createComparisonGraph(Network network) {
		return createComparisonGraph(createStudyGraph(network));
	}

	public static UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> createComparisonGraph(Hypergraph<Treatment, Study> studyGraph) {
		return FoldingTransformerFixed.foldHypergraphEdges(studyGraph, UndirectedSparseGraph.<Treatment, FoldedEdge<Treatment, Study>>getFactory());
	}

	private static Collection<Treatment> getTreatments(Study s) {
		Set<Treatment> treatments = new HashSet<Treatment>();
		for (Measurement m : s.getMeasurements()) {
			treatments.add(m.getTreatment());
		}
		return treatments;
	}
}