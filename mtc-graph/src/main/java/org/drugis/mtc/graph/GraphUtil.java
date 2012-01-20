package org.drugis.mtc.graph;

import java.util.Collection;
import java.util.LinkedList;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Tree;

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
			E e = toAdd.pop();
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
}
