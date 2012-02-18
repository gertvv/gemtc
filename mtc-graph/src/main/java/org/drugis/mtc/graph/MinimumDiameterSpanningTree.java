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

package org.drugis.mtc.graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.algorithms.shortestpath.ShortestPath;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.UndirectedGraph;

public class MinimumDiameterSpanningTree<V, E> {
	private final UndirectedGraph<V, E> d_graph;
	private final Transformer<E, Number> d_edgeLength;
	private final Distance<V> d_distance;
	private final ShortestPath<V, E> d_shortestPath;
	private final Comparator<V> d_vertexComparator;

	/**
	 * Minimum diameter spanning tree of a unweighted graph.
	 */
	public MinimumDiameterSpanningTree(final UndirectedGraph<V, E> graph) {
		this(graph, new AbsoluteOneCenter.UnitLength<E>(), new DijkstraShortestPath<V, E>(graph));
	}

	/**
	 * Minimum diameter spanning tree of a unweighted graph.
	 */
	public MinimumDiameterSpanningTree(final UndirectedGraph<V, E> graph, Comparator<V> vertexComparator) {
		this(graph, new AbsoluteOneCenter.UnitLength<E>(), new DijkstraShortestPath<V, E>(graph), vertexComparator);
	}
	
	/**
	 * Minimum diameter spanning tree of a weighted graph.
	 */
	public MinimumDiameterSpanningTree(final UndirectedGraph<V, E> graph, final Transformer<E, Number> edgeLength) {
		this(graph, edgeLength, new DijkstraShortestPath<V, E>(graph, edgeLength));
	}
	
	/**
	 * Minimum diameter spanning tree of a weighted graph.
	 */
	public MinimumDiameterSpanningTree(final UndirectedGraph<V, E> graph, final Transformer<E, Number> edgeLength, 
			final DijkstraShortestPath<V, E> shortestPath) {
		this(graph, edgeLength, shortestPath, shortestPath);
	}
	
	/**
	 * Minimum diameter spanning tree of a weighted graph.
	 */
	public MinimumDiameterSpanningTree(final UndirectedGraph<V, E> graph, final Transformer<E, Number> edgeLength,
			final DijkstraShortestPath<V, E> shortestPath, final Comparator<V> vertexComparator) {
		this(graph, edgeLength, shortestPath, shortestPath, vertexComparator);
	}
	
	/**
	 * Minimum diameter spanning tree of a weighted graph.
	 */
	public MinimumDiameterSpanningTree(final UndirectedGraph<V, E> graph, final Transformer<E, Number> edgeLength, 
			final Distance<V> distance, final ShortestPath<V, E> shortestPath) {
		this(graph, edgeLength, distance, shortestPath, null);
	}
	
	/**
	 * Minimum diameter spanning tree of a weighted graph.
	 */
	public MinimumDiameterSpanningTree(final UndirectedGraph<V, E> graph, final Transformer<E, Number> edgeLength, 
			final Distance<V> distance, final ShortestPath<V, E> shortestPath, Comparator<V> vertexComparator) {
		d_graph = graph;
		d_edgeLength = edgeLength;
		d_distance = distance;
		d_shortestPath = shortestPath;
		d_vertexComparator = vertexComparator;
	}

	public Tree<V, E> getMinimumDiameterSpanningTree() {
		PointOnEdge<V, E> center = new AbsoluteOneCenter<V, E>(d_graph, d_edgeLength, d_distance).getCenter();
		DelegateTree<V,E> tree = new DelegateTree<V, E>();
		
		// add the center edge
		// choose the one closest to the absolute one center, or the "first"
		// vertex according to the comparator if they are equally close.
		double dc = center.getDistance() - 0.5 * l(center.getEdge());
		int vc = d_vertexComparator == null ? 0 : d_vertexComparator.compare(center.getVertex0(), center.getVertex1());
		if (dc < 0.0 || (dc == 0.0 && vc <= 0)) {
			tree.setRoot(center.getVertex0());
			tree.addChild(center.getEdge(), center.getVertex0(), center.getVertex1());
		} else {
			tree.setRoot(center.getVertex1());
			tree.addChild(center.getEdge(), center.getVertex1(), center.getVertex0());
		}
		
		// add remaining edges based on shortest paths
		for (V v : d_graph.getVertices()) {
			if (v.equals(center.getVertex0()) || v.equals(center.getVertex1())) {
				continue;
			}
			double d0 = d_distance.getDistance(center.getVertex0(), v).doubleValue();
			double d1 = d_distance.getDistance(center.getVertex1(), v).doubleValue();
			if (d0 + center.getDistance() <= d1 + l(center.getEdge()) - center.getDistance()) {
				addPath(tree, center.getVertex0(), v);
			} else {
				addPath(tree, center.getVertex1(), v);
			}
		}

		return tree;
	}

	private void addPath(DelegateTree<V, E> tree, V u, V v) {
		Map<V, E> incomingEdgeMap = d_shortestPath.getIncomingEdgeMap(u);
		while (!v.equals(u)) {
			E e = incomingEdgeMap.get(v);
			List<V> incidentVertices = new ArrayList<V>(d_graph.getIncidentVertices(e));
			V w = incidentVertices.get(0).equals(v) ? incidentVertices.get(1) : incidentVertices.get(0);
			if (!tree.containsEdge(e)) {
				tree.addChild(e, w, v);
			}
			v = w;
		}
	}

	private double l(E e) {
		return d_edgeLength.transform(e).doubleValue();
	}
}
