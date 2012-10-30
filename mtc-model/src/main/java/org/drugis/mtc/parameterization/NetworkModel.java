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
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.drugis.mtc.graph.GraphUtil;
import org.drugis.mtc.model.Measurement;
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
import edu.uci.ics.jung.graph.util.Pair;

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
		final UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> graph = FoldingTransformerFixed.foldHypergraphEdges(studyGraph, UndirectedSparseGraph.<Treatment, FoldedEdge<Treatment, Study>>getFactory());
		standardizeEdgeVertexOrder(graph);
		return graph;
	}

	private static void standardizeEdgeVertexOrder(final UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> graph) {
		for (FoldedEdge<Treatment, Study> edge : new ArrayList<FoldedEdge<Treatment, Study>>(graph.getEdges())) {
			final List<Treatment> vertices = new ArrayList<Treatment>(graph.getIncidentVertices(edge));
			Collections.sort(vertices, new TreatmentComparator());
			graph.removeEdge(edge);
			final FoldedEdge<Treatment, Study> edge2 = new FoldedEdge<Treatment, Study>(vertices.get(0), vertices.get(1));
			edge2.getFolded().addAll(edge.getFolded());
			graph.addEdge(edge2, vertices);
		}
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

	public static Measurement findMeasurement(final Study study, final Treatment treatment) {
		return CollectionUtils.find(study.getMeasurements(), new Predicate<Measurement>() {
			public boolean evaluate(Measurement m) {
				return m.getTreatment().equals(treatment);
			}
		});
	}
	
	/**
	 * Get a sorted list of treatments in the study. 
	 */
	public static List<Treatment> getTreatments(final Study study) {
		List<Treatment> treatments = new ArrayList<Treatment>(study.getTreatments());
		Collections.sort(treatments, TreatmentComparator.INSTANCE);
		return treatments;
	}
	
	/**
	 * Apply a transformation to all (t_1, t_2) pairs of treatments where indexOf(t_1) < indexOf(t_2).
	 */
	public static <O> List<O> transformTreatmentPairs(Network network, Transformer<Pair<Treatment>, ? extends O> transformer) {
		int n = network.getTreatments().size();
		List<O> list = new ArrayList<O>();
		for (int i = 0; i < n - 1; ++i) {
			for (int j = i + 1; j < n; ++j) {
				final Treatment ti = network.getTreatments().get(i);
				final Treatment tj = network.getTreatments().get(j);
				list.add(transformer.transform(new Pair<Treatment>(ti, tj)));
			}
		}
		return list;		
	}
}