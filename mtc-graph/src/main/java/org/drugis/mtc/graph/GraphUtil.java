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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.UndirectedGraph;

public class GraphUtil {
	public static <V, E> void addVertices(Graph<V, E> graph, Collection<V> vertices) {
		for (V v : vertices) {
			graph.addVertex(v);
		}
	}
	
	public static <V, E> void copyGraph(Graph<V, E> source, Graph<V, E> target) {
		addVertices(target, source.getVertices());
		for (E e : source.getEdges()) {
			target.addEdge(e, source.getIncidentVertices(e), source.getEdgeType(e));
		}
	}
	
	public static <V, E> void copyTree(Tree<V, E> source, Tree<V, E> target) {
		target.addVertex(source.getRoot());
		LinkedList<E> toAdd = new LinkedList<E>(source.getChildEdges(source.getRoot()));
		while (!toAdd.isEmpty()) {
			E e = toAdd.removeFirst();
			target.addEdge(e, source.getSource(e), source.getDest(e));
			toAdd.addAll(source.getChildEdges(source.getDest(e)));
		}
	}
	
	/**
	 * Test whether w is a descendent of v in tree t.
	 */
	public static <V, E> boolean isDescendant(Tree<V, E> t, V v, V w) {
		if (v.equals(w)) {
			return true;
		}
		
		for (E e : t.getOutEdges(v)) {
			if (isDescendant(t, t.getDest(e), w)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Test whether the given graph is connected.
	 * For directed graphs, tests weak connectivity, i.e. it tests whether the
	 * graph is connected when all directed edges are replaced by undirected
	 * ones.
	 */
	public static <V, E> boolean isWeaklyConnected(Hypergraph<V, E> graph) {
		if (graph.getVertexCount() == 0) {
			return true;
		} else {
			HashSet<V> visited = new HashSet<V>(); // already visited vertices
			LinkedList<V> fringe = new LinkedList<V>(); // directly reachable vertices
			fringe.add(graph.getVertices().iterator().next()); // start at an arbitrary vertex
			while (!fringe.isEmpty()) {
				V v = fringe.removeFirst();
				if (!visited.contains(v)) {
					visited.add(v);
					fringe.addAll(graph.getNeighbors(v));
				}
				if (visited.size() == graph.getVertexCount()) {
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * Test whether the given vertices are connected in the hypergraph.
	 * For directed graphs, tests weak connectivity, i.e. it tests whether the
	 * vertices are connected when all directed edges are replaced by
	 * undirected ones.
	 */
	public static <V,E> boolean areVerticesWeaklyConnected(Hypergraph<V, E> studyGraph, V first, V second) {
		Set<V> visited = new HashSet<V>();
		LinkedList<V> fringe = new LinkedList<V>();
		fringe.add(first);
		while (!fringe.isEmpty()) {
			V v = fringe.removeFirst();
			if (v.equals(second)) {
				return true;
			}
			visited.add(v);
			HashSet<V> neighbors = new HashSet<V>(studyGraph.getNeighbors(v));
			neighbors.removeAll(visited);
			neighbors.removeAll(fringe);
			fringe.addAll(neighbors);
		}
		return false;
	}
	
	/**
	 * Determine whether the graph is a simple cycle: it is connected, and its
	 * edges form a closed simple path (no repeated edges or vertices, except
	 * the first and last).
	 */
	public static <V, E> boolean isSimpleCycle(UndirectedGraph<V, E> graph) {
		if (graph.getVertexCount() < 3 || graph.getEdgeCount() < graph.getVertexCount()) {
			return false;
		}
		V v = graph.getVertices().iterator().next();
		Set<V> visited = new HashSet<V>();
		visited.add(v);
		while (graph.getNeighborCount(v) == 2 && !visited.containsAll(graph.getNeighbors(v))) {
			Iterator<V> iterator = graph.getNeighbors(v).iterator();
			V v1 = iterator.next();
			V v2 = iterator.next();
			if (!visited.contains(v1)) {
				v = v1;
				visited.add(v1);
			} else {
				v = v2;
				visited.add(v2);
			}
		}
		return visited.containsAll(graph.getVertices());
	}
	
	/**
	 * Find the closest common ancestor of u and v in the given tree. May be u or v itself.
	 */
	public static <V, E> V findCommonAncestor(Tree<V, E> tree, V u, V v) {
		while (tree.getDepth(u) > tree.getDepth(v)) {
			u = tree.getParent(u);
		}
		while (tree.getDepth(v) > tree.getDepth(u)) {
			v = tree.getParent(v);
		}
		while (!u.equals(v)) {
			v = tree.getParent(v);
			u = tree.getParent(u);
		}
		return v;
	}
	
	/**
	 * Find the (unique, simple) path from u to v in the given tree.
	 */
	public static <V, E> List<V> findPath(Tree<V, E> tree, V u, V v) {
		LinkedList<V> path = new LinkedList<V>();
		while (tree.getDepth(u) > tree.getDepth(v)) {
			path.add(u);
			u = tree.getParent(u);
		}
		int idx = path.size();
		while (tree.getDepth(v) > tree.getDepth(u)) {
			path.add(idx, v);
			v = tree.getParent(v);
		}
		while (!u.equals(v)) {
			path.add(idx, v);
			path.add(idx, u);
			++idx;
			v = tree.getParent(v);
			u = tree.getParent(u);
		}
		path.add(idx, u);
		return path;
	}
}
