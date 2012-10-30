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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Find the absolute 1-center of an undirected graph.
 * 
 * Let e = (u,v) be an edge of the graph, and l(e) the length of e.
 * Let x(e) be a point along the edge, with t = t(x(e)) \in [0, l(e)] the distance of x(e) from u.
 * Then, for any point x on G, d(v, x) is the length of the shortest path in G between the vertex v and point x.
 * Define F(x) = \max_{v \in V} d(v, x), and x* = \arg\min_{x on G} F(x).
 * x* is the absolute 1-center of G and F(x*) is the absolute 1-radius of G.
 *
 * Algorithm from <a href="http://www.jstor.org/pss/2100910">Kariv and Hakimi (1979), SIAM J Appl Math 37 (3): 513-538</a>.
 * 
 * (Note: in the paper, the weight of an edge is referred to as its length l(e), and vertices can also have weights w(v).
 * This class only implements the vertex-unweighted algorithm. Edge-weighted graphs are supported, however.)
 */
public class AbsoluteOneCenter<V, E> {
	public static class UnitLength<E> implements Transformer<E, Number> {
		public Number transform(E input) {
			return 1.0;
		}

	}

	private final UndirectedGraph<V, E> d_graph;
	private final Distance<V> d_distance;
	private final Transformer<E, Number> d_edgeLength;
	private Comparator<V> d_comparator;

	/**
	 * Absolute 1-center of an unweighted graph.
	 */
	public AbsoluteOneCenter(final UndirectedGraph<V, E> graph) {
		this(graph, new UnitLength<E>(), new DijkstraShortestPath<V, E>(graph));
	}
	
	/**
	 * Absolute 1-center of an unweighted graph.
	 */
	public AbsoluteOneCenter(final UndirectedGraph<V, E> graph, final Comparator<V> comparator) {
		this(graph, new UnitLength<E>(), new DijkstraShortestPath<V, E>(graph), comparator);
	}
	
	/**
	 * Absolute 1-center of a weighted graph.
	 */
	public AbsoluteOneCenter(final UndirectedGraph<V, E> graph, final Transformer<E, Number> edgeLength) {
		this(graph, edgeLength, new DijkstraShortestPath<V, E>(graph));
	}
	
	/**
	 * Absolute 1-center of a weighted graph.
	 */
	public AbsoluteOneCenter(final UndirectedGraph<V, E> graph, final Transformer<E, Number> edgeLength, final Comparator<V> comparator) {
		this(graph, edgeLength, new DijkstraShortestPath<V, E>(graph), comparator);
	}
	
	/**
	 * Absolute 1-center of an weighted graph.
	 */
	public AbsoluteOneCenter(final UndirectedGraph<V, E> graph, final Transformer<E, Number> edgeLength, final Distance<V> distance) {
		this(graph, edgeLength, distance, null);
	}
	
	/**
	 * Absolute 1-center of an weighted graph.
	 */
	public AbsoluteOneCenter(final UndirectedGraph<V, E> graph, final Transformer<E, Number> edgeLength, final Distance<V> distance, final Comparator<V> comparator) {
		d_graph = graph;
		d_edgeLength = edgeLength;
		d_distance = distance;
		d_comparator = comparator;
	}

	public PointOnEdge<V, E> getCenter() {
		LocalCenter<V, E> localCenter = new LocalCenter<V, E>(d_graph, d_edgeLength, d_distance, d_comparator);
		
		Center<V, E> c = null;
		for (E e : d_graph.getEdges()) {
			Center<V, E> lc = localCenter.transform(e);
			if (c == null || lc.getRadius() < c.getRadius() ||
					(lc.getRadius() == c.getRadius() && compareEdge(lc.getEdge(), c.getEdge()) < 0)) {
				c = lc;
			}
		}
		
		return c;
	}

	private int compareEdge(E e1, E e2) {
		if (d_comparator == null) {
			return 0;
		}
		Pair<V> x = new Pair<V>(d_graph.getIncidentVertices(e1));
		Pair<V> y = new Pair<V>(d_graph.getIncidentVertices(e2));
		final int compareFirst = d_comparator.compare(x.getFirst(), y.getFirst());
		if (compareFirst != 0) {
			return compareFirst;
		}
		return d_comparator.compare(x.getSecond(), y.getSecond());
	}

	/**
	 * Given an edge e = (e0, e1) and vertices u and v, find the distance t* \in (0, l) from e0 where D_e(u, t*) = D_e(v, t*),
	 * and D_e(u, t*) and D_e(v, t*) have opposite signs (if it exists).
	 */
	static <V> Double intersect(final Distance<V> distance, final V e0, final V e1, final double l, final V u, final V v) {
		final double lu = distance.getDistance(e0, u).doubleValue();
		final double ru = distance.getDistance(e1, u).doubleValue();
		final double lv = distance.getDistance(e0, v).doubleValue();
		final double rv = distance.getDistance(e1, v).doubleValue();

		if (lu == lv || ru == rv) { // they coincide or intersect only at the edge
			return null;
		} else if (lu > lv && ru > rv) { // u dominates v
			return null;
		} else if (lu < lv && ru < rv) { // v dominates u
			return null;
		}
		else {
			final double t1 = 0.5 * (rv - lu + l);
			final double t2 = 0.5 * (ru - lv + l);
			if (t1 + lu <= l - t1 + ru) {
				return t1;
			} else {
				return t2;
			}
		}
	}
	
	/**
	 * Sort the vertices of the graph according to non-increasing distance from a vertex v.
	 * @param distance Distance function for the graph.
	 * @param v The vertex.
	 * @param comparator Comparator to break ties between vertices of equal distance.
	 * Used to make the results fully deterministic, can be null if this is not required.
	 */
	static <V> List<V> distanceOrderedVertices(final Distance<V> distance, final V v, final Comparator<V> comparator) {
		final Map<V, Number> map = distance.getDistanceMap(v);
		List<V> list = new ArrayList<V>(map.keySet());
		Collections.sort(list, new Comparator<V>() {
			public int compare(V a, V b) {
				if (map.get(a).equals(map.get(b))) {
					return comparator == null ? 0 : comparator.compare(a, b);
				} else {
					return ((Double)map.get(b).doubleValue()).compareTo(map.get(a).doubleValue());
				}
			}
		});
		return list;
	}

	static class Center<V, E> extends PointOnEdge<V, E> {
		private final double d_r;

		public Center(E e, V v0, V v1, double t, double r) {
			super(e, v0, v1, t);
			d_r = r;
		}
		
		public Center(PointOnEdge<V, E> x, double r) {
			this(x.getEdge(), x.getVertex0(), x.getVertex1(), x.getDistance(), r);
		}

		public double getRadius() {
			return d_r;
		}
	}
	
	/**
	 * Find the local center along an edge e = (u, v) of an undirected graph G.
	 * Algorithm 2.3 (pages 521-522) of <a href="http://www.jstor.org/pss/2100910">Kariv and Hakimi (1979)</a>.
	 * 
	 * A local center of G on edge e is a point x*(e) on e, such that F(x*(e)) = min_{x(e) on e} F(x(e)).
	 */
	static class LocalCenter<V, E> implements Transformer<E, Center<V, E>> {
		private final UndirectedGraph<V, E> d_graph;
		private final Distance<V> d_distance;
		private final Transformer<E, Number> d_edgeLength;
		/**
		 * The lists L(v) of vertices sorted according to non-increasing distance from v.
		 */
		private final Map<V, List<V>> d_orderedVertices = new HashMap<V, List<V>>();
		
		/**
		 * @param graph The graph to calculate local centers for.
		 * @param edgeLength Edge length function l(e).
		 * @param distance Distance function defined on the given graph.
		 */
		public LocalCenter(final UndirectedGraph<V, E> graph, final Transformer<E, Number> edgeLength, final Distance<V> distance) {
			this(graph, edgeLength, distance, null);
		}
		
		/**
		 * @param graph The graph to calculate local centers for.
		 * @param edgeLength Edge length function l(e).
		 * @param distance Distance function defined on the given graph.
		 * @param vertexComparator Used to break ties in distance.
		 */
		public LocalCenter(final UndirectedGraph<V, E> graph, final Transformer<E, Number> edgeLength, final Distance<V> distance, Comparator<V> vertexComparator) {
			d_graph = graph;
			d_edgeLength = edgeLength;
			d_distance = distance;
			
			// Pre-processing: calculate the lists L(v)
			for (V v : d_graph.getVertices()) {
				d_orderedVertices.put(v, distanceOrderedVertices(distance, v, vertexComparator));
			}
		}
		
		/**
		 * Distance between u and v.
		 */
		private double d(V u, V v) {
			return d_distance.getDistance(u, v).doubleValue();
		}
		
        /**
         * distance between vertex v and point x
         */
		private double de(final PointOnEdge<V, E> x, final V v) {
			return Math.min(x.getDistance() + d(x.getVertex0(), v), l(x) - x.getDistance() + d(x.getVertex1(), v));
		}

		/**
		 * Length of the edge x is on.
		 */
		private double l(PointOnEdge<V, E> x) {
			return d_edgeLength.transform(x.getEdge()).doubleValue();
		}
		
		/**
		 * Point that coincides with the left-hand side vertex.
		 */
		private PointOnEdge<V, E> xr(PointOnEdge<V, E> x) {
			return new PointOnEdge<V, E>(x.getEdge(), x.getVertex0(), x.getVertex1(), 0.0);
		}

		/**
		 * Point that coincides with the right-hand side vertex.
		 */
		private PointOnEdge<V, E> xs(PointOnEdge<V, E> x) {
			return new PointOnEdge<V, E>(x.getEdge(), x.getVertex0(), x.getVertex1(), l(x));
		}
		
		/**
		 * Point that coincides with the right-hand side vertex.
		 */
		private PointOnEdge<V, E> xt(PointOnEdge<V, E> x, double t) {
			return new PointOnEdge<V, E>(x.getEdge(), x.getVertex0(), x.getVertex1(), t);
		}
		
		
		/**
		 * Calculate the local center of an edge.
		 * @param edge The edge to calculate the local center of.
		 * @return The local center (point on the given edge).
		 */
		public Center<V, E> transform(final E edge) {
//			System.out.println("Edge " + edge + " start");
			
			final V vr = d_graph.getEndpoints(edge).getFirst();
			final V vs = d_graph.getEndpoints(edge).getSecond();
			final PointOnEdge<V, E> xr = new PointOnEdge<V, E>(edge, vr, vs, 0.0);
			final PointOnEdge<V, E> xs = xs(xr);

			// Step 1: treatment of t = 0 and t = l(e)
			Center<V, E> c = null;
			double dr = de(xr, d_orderedVertices.get(vr).get(0));
			double ds = de(xs, d_orderedVertices.get(vs).get(0));
			if (dr <= ds) {
				c = new Center<V, E>(xr, dr);
			} else {
				c = new Center<V, E>(xs, ds);
			}

			if (d_orderedVertices.get(vr).get(0) == d_orderedVertices.get(vs).get(0)) {
				return c;
			} else {
				return step3(c, d_orderedVertices.get(vr).get(0), 0);
			}
		}

		/**
		 * Step 3: treatment of vertices v s.t. D_e(v, 0) = D_e(v_1, 0)
		 * @param c The suspected center
		 * @param l List of vertices to consider
		 * @param i Index of the last-treated vertex
		 * @return The local center
		 */
		private Center<V, E> step3(Center<V, E> c, V vm, int i) {
//			System.out.println("step3: " + (i + 1));
			V v = d_orderedVertices.get(c.getVertex0()).get(i + 1); // guaranteed to succeed
            if (de(xs(c), v) != de(xs(c), vm)) {
            	return step4(c, vm, i + 1);
            } else if (de(xr(c), v) > de(xs(c), vm)) {
            	return step3(c, v, i + 1); // v_m <- v*
            } else {
            	return step3(c, vm, i + 1);
            }
		}

		private Center<V, E> step4(Center<V, E> c, V vm, int i) {
			V vbar = vm;
			if (isLastVertex(i)) {
				return step8(c, vbar);
			} else {
				vm = d_orderedVertices.get(c.getVertex0()).get(i);
				return step5(c, vm, vbar, i);
			}
		}

		private boolean isLastVertex(int i) {
			return i == d_graph.getVertexCount() - 1;
		}

		/**
		 * Step 5: find all vertices v s.t. D_e(v, 0) = D_e(v_i, 0) and find the corresponding v_m.
		 */
		private Center<V, E> step5(Center<V, E> c, V vm, V vbar, int i) {
//			System.out.println("step5: " + (i + 1));
			V v = d_orderedVertices.get(c.getVertex0()).get(i + 1); // guaranteed to succeed
            if (de(xr(c), v) != de(xr(c), vm)) {
            	return step6(c, vm, vbar, i + 1);
            } else if (de(xs(c), v) > de(xs(c), vm)) {
            	return step5(c, v, vbar, i + 1); // v_m <- v*
            } else {
            	return step5(c, vm, vbar, i + 1);
            }
		}

		/**
		 * Step 6: treatment of the point t_m.
		 */
		private Center<V, E> step6(Center<V, E> c, V vm, V vbar, int i) {
			Double tm = intersect(d_distance, c.getVertex0(), c.getVertex1(), l(c), vm, vbar);
//			System.out.println("Proposed center: " + c + ", intersect " + vm + " and " + vbar + " --> " + tm);
			if (tm == null) {
				return step7(c, vm, vbar, i);
			} else {
				PointOnEdge<V, E> xt = xt(c, tm);
				Center<V, E> ct = new Center<V, E>(xt, de(xt, vm));
				if (ct.getRadius() < c.getRadius()) {
					return step7(ct, vm, vbar, i);
				} else {
					return step7(c, vm, vbar, i);
				}
			}
		}

		/**
		 * Step 7: proceed to the next vertex.
		 */
		private Center<V, E> step7(Center<V, E> c, V vm, V vbar, int i) {
			if (de(xs(c), vm) > de(xs(c), vbar)) {
				vbar = vm;
			}
			
			if (isLastVertex(i)) {
				return step8(c, vbar);
			} else {
				return step5(c, d_orderedVertices.get(c.getVertex0()).get(i), vbar, i);
			}
		}

		private Center<V, E> step8(Center<V, E> c, V vbar) {
			V vn = d_orderedVertices.get(c.getVertex0()).get(d_graph.getVertexCount() - 1); // == v_r ?
			Double tm = intersect(d_distance, c.getVertex0(), c.getVertex1(), l(c), vn, vbar);
//			System.out.println("Proposed center: " + c + ", intersect " + vn + " and " + vbar + " --> " + tm);
			if (tm == null) {
				return c;
			} else {
				PointOnEdge<V, E> xt = xt(c, tm);
				Center<V, E> ct = new Center<V, E>(xt, de(xt, vn));
				if (ct.getRadius() < c.getRadius()) {
					return ct;
				} else {
					return c;
				}
			}
		}
	}
}
