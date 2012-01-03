package org.drugis.mtc.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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

		@Override
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
			@Override
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
}
