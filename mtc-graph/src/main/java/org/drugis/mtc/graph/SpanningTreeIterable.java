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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Tree;

/**
 * A java.lang.Iterable that finds all spanning trees of a graph.
 * @param <V> Vertex type.
 * @param <E> Edge type.
 */
public class SpanningTreeIterable<V, E> implements Iterable<Tree<V, E>> {
	private static class ProblemState<V, E> {
		public final DirectedGraph<V, E> graph; // The current graph (potentially a modified version)
		public final Tree<V, E> tree; // The tree built so far
		public final List<E> fringe; // The fringe of edges to explore next
		public final boolean widen; // Whether or not to widen the search
		
		public ProblemState(DirectedGraph<V, E> graph, Tree<V, E> tree, List<E> fringe, boolean bridgeTest) {
			this.graph = graph;
			this.tree = tree;
			this.fringe = fringe;
			this.widen = bridgeTest;
		}
	}
	
	public class SpanningTreeIterator implements Iterator<Tree<V, E>> {
		private Tree<V, E> d_last = null;
		private Tree<V, E> d_current = null;
		private LinkedList<ProblemState<V, E>> d_queue = new LinkedList<ProblemState<V,E>>();
		
		public SpanningTreeIterator() {
			DelegateTree<V, E> initialTree = new DelegateTree<V, E>();
			initialTree.setRoot(d_root);
			d_queue.add(new ProblemState<V, E>(d_graph, initialTree, getOutEdges(d_graph, d_root), false));
		}

		public boolean hasNext() {
			if (d_current == null) {
				d_current = findNext();
			}
			return d_current != null;
		}

		public Tree<V, E> next() {
			if (hasNext()) {
				d_last = d_current;
				d_current = null;
				return d_last;
			} else {
				return null;
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		/**
		 * Find the next spanning tree.
		 */
		private Tree<V, E> findNext() {
			while (!d_queue.isEmpty()) {
				ProblemState<V, E> head = d_queue.removeFirst();
				d_queue.addAll(0, successors(head));
				if (isGoal(head)) {
					return head.tree;
				}
			}
			return null;
		}
		
		/**
		 * Test whether the given state is a goal state (spanning tree found).
		 */
		private boolean isGoal(ProblemState<V, E> state) {
			return !state.widen && state.tree.getVertexCount() == d_graph.getVertexCount();
		}
		
		/**
		 * Given the current state, determine the next state(s).
		 */
		private List<ProblemState<V, E>> successors(ProblemState<V, E> state) {
			if (state.fringe.isEmpty()) {
				return Collections.emptyList();
			} else if (state.widen) {
				ProblemState<V,E> succ = widenSucc(state);
				return succ == null ? Collections.<ProblemState<V, E>>emptyList() : Collections.singletonList(succ);
			} else {
				List<ProblemState<V, E>> list = new ArrayList<ProblemState<V, E>>();
				list.add(deepenSucc(state));
				list.add(new ProblemState<V, E>(state.graph, state.tree, state.fringe, true));
				return list;
			}
		}
		
		/**
		 * Widen the search.
		 * Determine whether to widen the search (if e is a bridge, all spanning
		 * trees will include it, hence we don't need to widen the search).
		 */
		private ProblemState<V, E> widenSucc(ProblemState<V, E> state) {
			E e = state.fringe.get(0);
			DirectedGraph<V, E> modGraph = new DirectedSparseGraph<V, E>();
			GraphUtil.copyGraph(state.graph, modGraph);
			modGraph.removeEdge(e);
			if (bridgeTest(modGraph, state.graph.getDest(e))) {
				return null;
			} else {
				List<E> newFringe = state.fringe.subList(1, state.fringe.size());
				return new ProblemState<V, E>(modGraph, state.tree, newFringe, false);
			}
		}

		/**
		 * Search deeper.
		 */
		private ProblemState<V, E> deepenSucc(ProblemState<V, E> state) {
			E e = state.fringe.get(0);
			V v = state.graph.getDest(e);
			DelegateTree<V, E> newTree = new DelegateTree<V, E>();
			GraphUtil.copyTree(state.tree, newTree);
			newTree.addChild(e, state.graph.getSource(e), v);
			List<E> newFringe = new ArrayList<E>();
			// Add to the fringe forall_w (v, w) \in g, w \not\in t
			for (E x : getOutEdges(state.graph, v)) {
				if (!newTree.containsVertex(state.graph.getDest(x))) {
					newFringe.add(x);
				}
			}
			// Then, remove \forall_w (w, v) from F
			for (E x : state.fringe.subList(1, state.fringe.size())) {
				if (!state.graph.getDest(x).equals(v)) {
					newFringe.add(x);
				}
			}
			return new ProblemState<V, E>(state.graph, newTree, newFringe, false);
		}
		
		/**
		 * Test whether v is a bridge in g, using the last found spanning tree.
		 */
		private boolean bridgeTest(DirectedGraph<V, E> graph, V dest) {
			if (d_last == null) {
				throw new IllegalStateException();
			}
			for (E e : graph.getInEdges(dest)) {
				if (!GraphUtil.isDescendant(d_last, dest, graph.getSource(e))) {
					return false;
				}
			}
			return true;
		}
		
		/**
		 * Sort the edges to guarantee the same tree is always returned.
		 */
		private List<E> getOutEdges(final DirectedGraph<V, E> graph, final V v0) {
			final ArrayList<E> edges = new ArrayList<E>(graph.getOutEdges(v0));
			if (d_vertexComparator != null) {
				Collections.sort(edges, new Comparator<E>() {
					public int compare(E e1, E e2) {
						V v1 = graph.getDest(e1);
						V v2 = graph.getDest(e2);
						return d_vertexComparator.compare(v1, v2); // Otherwise: natural order
					}});
			}
			return edges;
		}
	}

	private final DirectedGraph<V, E> d_graph;
	private final V d_root;
	private final Comparator<V> d_vertexComparator;

	/**
	 * Create a java.lang.Iterable for all spanning trees of the given graph, rooted at the given root.
	 */
	public SpanningTreeIterable(DirectedGraph<V, E> graph, V root) {
		this(graph, root, null);
	}

	/**
	 * Create a java.lang.Iterable for all spanning trees of the given graph, rooted at the given root.
	 */
	public SpanningTreeIterable(DirectedGraph<V, E> graph, V root, Comparator<V> vertexComparator) {
		d_graph = graph;
		d_root = root;
		d_vertexComparator = vertexComparator;
	}
	
	/**
	 * Iterate over the spanning trees of the graph.
	 */
	public Iterator<Tree<V, E>> iterator() {
		return new SpanningTreeIterator();
	}
}
