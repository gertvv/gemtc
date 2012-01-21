package org.drugis.mtc.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.SetHypergraph;

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

	private static Collection<Treatment> getTreatments(Study s) {
		Set<Treatment> treatments = new HashSet<Treatment>();
		for (Measurement m : s.getMeasurements()) {
			treatments.add(m.getTreatment());
		}
		return treatments;
	}
}