package org.drugis.mtc.parameterization;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.drugis.mtc.graph.GraphUtil;
import org.drugis.mtc.model.Treatment;

import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;

public class Partition {
	private final Set<Part> d_parts;

	public Partition(Collection<Part> parts) {
		d_parts = new HashSet<Part>(parts);
		if (!validPartition(d_parts)) {
			throw new IllegalArgumentException("Given parts do not form a valid partition");
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Partition) {
			Partition other = (Partition) obj;
			return other.d_parts.equals(d_parts);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return d_parts.hashCode();
	}

	private static boolean validPartition(Set<Part> parts) {
		if (parts.isEmpty()) {
			return false;
		}
		if (parts.size() == 1) {
			return parts.iterator().next().getTreatments().size() == 1;
		}
		if (parts.size() == 2) {
			Iterator<Part> iterator = parts.iterator();
			Part p1 = iterator.next();
			Part p2 = iterator.next();
			return p1.getTreatments().equals(p2.getTreatments());
		}
		
		// now try to assemble the parts into a cycle
		UndirectedGraph<Treatment, Part> graph = buildGraph(parts);
		return graph == null ? false : GraphUtil.isSimpleCycle(graph);
	}

	private static UndirectedGraph<Treatment, Part> buildGraph(Set<Part> parts) {
		UndirectedGraph<Treatment, Part> graph = new UndirectedSparseGraph<Treatment, Part>();
		for (Part p : parts) {
			if (p.getTreatments().size() != 2) { // FIXME: add test for this.
				return null;
			}
			Iterator<Treatment> iterator = p.getTreatments().iterator();
			Treatment t1 = iterator.next();
			Treatment t2 = iterator.next();
			graph.addEdge(p, t1, t2);
		}
		return graph;
	}

	public Partition reduce() {
		if (d_parts.size() < 3) {
			return this;
		}
		
		UndirectedGraph<Treatment, Part> graph = buildGraph(d_parts);
		
		// Start at an arbitrary part
		Part p = d_parts.iterator().next(); // Starting part
		Pair<Treatment> treatments = new Pair<Treatment>(p.getTreatments());
		Set<Part> visited = new HashSet<Part>(Collections.singleton(p)); // Visited parts
		
		// Go as far to the right as possible
		Treatment tRight = walk(graph, p, treatments.getFirst(), visited);
		if (tRight.equals(treatments.getFirst())) {
			return new Partition(Collections.singleton(new Part(tRight, tRight, p.getStudies())));
		}
		// Go as far to the left as possible
		Treatment tLeft = walk(graph, p, treatments.getSecond(), visited);
		
		// Now start going around the cycle starting at tRight, until we get to tLeft.
		Set<Part> reduced = new HashSet<Part>();
		reduced.add(new Part(tRight, tLeft, p.getStudies()));		
		do {
			p = nextPart(graph, visited, tRight);
			Treatment tNext = walk(graph, p, tRight, visited);
			reduced.add(new Part(tRight, tNext, p.getStudies()));
			tRight = tNext;
		} while (!tRight.equals(tLeft));
		
		return new Partition(reduced);
	}

	/**
	 * Walk the partition cycle, starting from the part p0 with treatments {t_0, t_1}, going in the direction t_0 --> t_1.
	 * Stop walking when either t_k == t_0 or when p_k and p_0 have different studies.
	 * @param graph The (simple cycle) graph to walk.
	 * @param p0 The starting edge p_0.
	 * @param t0 The starting vertex t_0.
	 * @param visited (OUTPUT) set of visited edges. This is an output parameter (the set is written to).
	 */
	private Treatment walk(final UndirectedGraph<Treatment, Part> graph, final Part p0, final Treatment t0, Set<Part> visited) {
		Treatment tPrev = t0; // Previously visited vertex
		Treatment tCurr = otherTreatment(p0, t0); // Current vertex
		Part pNext = nextPart(graph, tPrev, tCurr); // Next edge to consider
		while (p0.getStudies().equals(pNext.getStudies()) && !t0.equals(tCurr)) {
			tPrev = tCurr;
			tCurr = otherTreatment(pNext, tCurr);
			visited.add(pNext); // We now considered pNext
			pNext = nextPart(graph, tPrev, tCurr); // So move on to the next part
		}
		return tCurr;
	}
	
	/**
	 * Get the treatment of this part that is not equal to the given treatment
	 * @param p Part with exactly 2 treatments.
	 * @param t Treatment not to return.
	 * @return The treatment s \in p, s != t.
	 */
	private static Treatment otherTreatment(Part p, Treatment t) {
		Pair<Treatment> treatments = new Pair<Treatment>(p.getTreatments());
		if (treatments.getFirst().equals(t)) {
			return treatments.getSecond();
		} else {
			return treatments.getFirst();
		}
	}

	/**
	 * Get the next part in the cycle, given we were walking from tPrev to tCurr.
	 * @param g The (simple cycle) graph to walk.
	 * @param tPrev The previously visited vertex.
	 * @param tCurr The current vertex.
	 * @return The edge to the next vertex.
	 */
	private static Part nextPart(UndirectedGraph<Treatment, Part> g, Treatment tPrev, Treatment tCurr) {
		Pair<Part> parts = new Pair<Part>(g.getIncidentEdges(tCurr));
		if (parts.getFirst().getTreatments().contains(tPrev)) {
			return parts.getSecond();
		} else {
			return parts.getFirst();
		}
	}
	
	/**
	 * Get the non-visited edge connected to the given vertex.
	 * @param g The (simple cycle) graph to walk
	 * @param visited Set containing all visited edges.
	 * @param tCurr The current vertex. 
	 * @return The edge connected to tCurr that has not been visited.
	 */
	private static Part nextPart(UndirectedGraph<Treatment, Part> g, Set<Part> visited, Treatment tCurr) {
		Pair<Part> parts = new Pair<Part>(g.getIncidentEdges(tCurr));
		if (visited.contains(parts.getFirst())) {
			return parts.getSecond();
		} else {
			return parts.getFirst();
		}
	}
}
