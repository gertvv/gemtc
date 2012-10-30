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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.collections15.Transformer;
import org.drugis.mtc.graph.AbsoluteOneCenter.LocalCenter;
import org.junit.Test;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class AbsoluteOneCenterTest {
	static class EdgeLength implements Transformer<Integer, Number> {
		private final double[] d_l;
		public EdgeLength(final double[] l) {
			d_l = l;
		}

		public Number transform(final Integer e) {
			return d_l[e];
		}

	}

	private static final double EPSILON = 0.00000000001;

	@Test
	public void testIntersect() {
		final UndirectedGraph<Integer, Integer> g = lineGraph();

		// unit edge lengths only
		final Distance<Integer> distance1 = new DijkstraDistance<Integer, Integer>(g);
		assertEquals(0.5, AbsoluteOneCenter.intersect(distance1, 1, 2, 1.0, 0, 3), EPSILON);
		assertNull(AbsoluteOneCenter.intersect(distance1, 1, 2, 1.0, 0, 4));

		// non-unit lengths outside the edge of interest
		final Distance<Integer> distance2 = new DijkstraDistance<Integer, Integer>(g, new EdgeLength(new double[] {1.0, 1.0, 1.0, 0.5}));
		assertEquals(0.5, AbsoluteOneCenter.intersect(distance2, 1, 2, 1.0, 0, 3), EPSILON);
		assertEquals(0.75, AbsoluteOneCenter.intersect(distance2, 1, 2, 1.0, 0, 4), EPSILON);

		// non-unit lengths on the edge of interest
		final Distance<Integer> distance3 = new DijkstraDistance<Integer, Integer>(g, new EdgeLength(new double[] {1.0, 0.5, 1.0, 0.5}));
		assertEquals(0.25, AbsoluteOneCenter.intersect(distance3, 1, 2, 0.5, 0, 3), EPSILON);
		assertNull(AbsoluteOneCenter.intersect(distance3, 1, 2, 0.5, 0, 4));
	}

	private UndirectedGraph<Integer, Integer> lineGraph() {
		// Create a line graph with 5 vertices.
		final UndirectedGraph<Integer, Integer> g = new UndirectedSparseGraph<Integer, Integer>();
		for (int i = 0; i < 5; ++i) {
			g.addVertex(i);
			if (i > 0) {
				g.addEdge(i - 1, i - 1, i);
			}
		}
		return g;
	}

	@Test
	public void testVertexOrder() {
		final UndirectedGraph<Integer, Integer> g = lineGraph();
		final DijkstraDistance<Integer, Integer> distance = new DijkstraDistance<Integer, Integer>(g);

		assertEquals(Arrays.asList(4, 3, 2, 1, 0), AbsoluteOneCenter.distanceOrderedVertices(distance, 0, intComparator()));
		assertEquals(Arrays.asList(4, 3, 0, 2, 1), AbsoluteOneCenter.distanceOrderedVertices(distance, 1, intComparator()));
		assertEquals(Arrays.asList(0, 4, 1, 3, 2), AbsoluteOneCenter.distanceOrderedVertices(distance, 2, intComparator()));
	}

	private Comparator<Integer> intComparator() {
		final Comparator<Integer> comparator = new Comparator<Integer>() {
			public int compare(final Integer o1, final Integer o2) {
				return o1.compareTo(o2);
			}
		};
		return comparator;
	}

	@Test
	public void testLocalCenter() {
		final UndirectedGraph<Integer, Integer> g = lineGraph();
		final DijkstraDistance<Integer, Integer> distance = new DijkstraDistance<Integer, Integer>(g);
		final LocalCenter<Integer,Integer> center = new AbsoluteOneCenter.LocalCenter<Integer, Integer>(g, new EdgeLength(new double[] {1.0, 1.0, 1.0, 1.0}), distance);

		assertEquals(1.0, center.transform(1).getDistance(), EPSILON);
		assertEquals(2.0, center.transform(1).getRadius(), EPSILON);
		assertEquals(1.0, center.transform(0).getDistance(), EPSILON);
		assertEquals(3.0, center.transform(0).getRadius(), EPSILON);
		assertEquals(0.0, center.transform(2).getDistance(), EPSILON);
		assertEquals(2.0, center.transform(2).getRadius(), EPSILON);

		final EdgeLength edgeLength = new EdgeLength(new double[] {1.0, 1.0, 1.0, 0.5});
		final DijkstraDistance<Integer, Integer> distance2 = new DijkstraDistance<Integer, Integer>(g, edgeLength);
		final LocalCenter<Integer,Integer> center2 = new AbsoluteOneCenter.LocalCenter<Integer, Integer>(g, edgeLength, distance2);

		assertEquals(0.75, center2.transform(1).getDistance(), EPSILON);
		assertEquals(1.75, center2.transform(1).getRadius(), EPSILON);
		assertEquals(0.0, center2.transform(2).getDistance(), EPSILON);
		assertEquals(2.0, center2.transform(2).getRadius(), EPSILON);
	}

	@Test
	public void testSimpleVertexCenter() {
		final UndirectedGraph<String, Integer> g = new UndirectedSparseGraph<String, Integer>();
		g.addVertex("A");
		g.addVertex("B");
		g.addVertex("C");
		g.addVertex("D");
		g.addEdge(1, "A", "B");
		g.addEdge(2, "B", "C");
		g.addEdge(3, "B", "D");

		final AbsoluteOneCenter<String, Integer> absoluteOneCenter = new AbsoluteOneCenter<String, Integer>(g);
		final PointOnEdge<String, Integer> center = absoluteOneCenter.getCenter();
		
		assertTrue(center.getVertex0().equals("B") || center.getVertex1().equals("B"));
		if (center.getVertex0().equals("B")) {
			assertEquals(0.0, center.getDistance(), EPSILON);
		} else {
			assertEquals(1.0, center.getDistance(), EPSILON);
		}
	}

	@Test
	public void testSimplePointCenter() {
		final UndirectedGraph<String, Integer> g = new UndirectedSparseGraph<String, Integer>();
		g.addVertex("A");
		g.addVertex("B");
		g.addVertex("C");
		g.addVertex("D");
		g.addEdge(1, "A", "B");
		g.addEdge(2, "B", "C");
		g.addEdge(3, "C", "D");

		final AbsoluteOneCenter<String, Integer> absoluteOneCenter = new AbsoluteOneCenter<String, Integer>(g);
		final PointOnEdge<String, Integer> center = absoluteOneCenter.getCenter();
		assertEquals(2, center.getEdge().intValue());
		assertEquals("B", center.getVertex0());
		assertEquals("C", center.getVertex1());
		assertEquals(0.5, center.getDistance(), EPSILON);
	}


	@Test
	public void testCenterWithCycles() {
		final UndirectedGraph<String, Integer> g = new UndirectedSparseGraph<String, Integer>();
		g.addVertex("A");
		g.addVertex("B");
		g.addVertex("C");
		g.addVertex("D");
		g.addVertex("E");
		g.addEdge(1, "A", "B");
		g.addEdge(2, "A", "C");
		g.addEdge(3, "A", "D");
		g.addEdge(4, "A", "E");
		g.addEdge(5, "B", "C");
		g.addEdge(6, "C", "D");
		g.addEdge(7, "D", "E");

		final AbsoluteOneCenter<String, Integer> absoluteOneCenter = new AbsoluteOneCenter<String, Integer>(g);
		final PointOnEdge<String, Integer> center = absoluteOneCenter.getCenter();
		
		assertTrue(center.getVertex0().equals("A") || center.getVertex1().equals("A"));
		if (center.getVertex0().equals("A")) {
			assertEquals(0.0, center.getDistance(), EPSILON);
		} else {
			assertEquals(1.0, center.getDistance(), EPSILON);
		}		
	}

	// FIXME: add more tests for non-unit edge weights (lengths)
}
