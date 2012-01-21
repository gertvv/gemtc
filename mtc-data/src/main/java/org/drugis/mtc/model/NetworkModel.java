package org.drugis.mtc.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.algorithms.transformation.FoldingTransformer;
import edu.uci.ics.jung.graph.Graph;
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
	
	public static UndirectedGraph<Treatment, Collection<Study>> createComparisonGraph(Network network) {
		return createComparisonGraph(createStudyGraph(network));
	}

	public static UndirectedGraph<Treatment, Collection<Study>> createComparisonGraph(Hypergraph<Treatment, Study> studyGraph) {
		return (UndirectedGraph<Treatment, Collection<Study>>)FoldingTransformer.foldHypergraphEdges(studyGraph, 
				new Factory<Graph<Treatment, Collection<Study>>>() {
					public Graph<Treatment, Collection<Study>> create() {
						return new UndirectedSparseGraph<Treatment, Collection<Study>>();
					}
				});
	}

	private static Collection<Treatment> getTreatments(Study s) {
		Set<Treatment> treatments = new HashSet<Treatment>();
		for (Measurement m : s.getMeasurements()) {
			treatments.add(m.getTreatment());
		}
		return treatments;
	}
}