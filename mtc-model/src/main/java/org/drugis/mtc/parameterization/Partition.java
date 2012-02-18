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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.drugis.mtc.graph.GraphUtil;
import org.drugis.mtc.model.Treatment;

import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;

public class Partition {
	private final Set<Part> d_parts;
	private boolean d_reduced;

	public Partition(Collection<Part> parts) {
		d_parts = new HashSet<Part>(parts);
		if (!validPartition(d_parts)) {
			throw new IllegalArgumentException("Given parts do not form a valid partition");
		}
		d_reduced = false;
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
	
	@Override
	public String toString() {
		return "Partition{" + d_parts.toString() + "}";
	}
	
	/**
	 * Get an unmodifiable view of the parts in this Partition.
	 */
	public Set<Part> getParts() {
		return Collections.unmodifiableSet(d_parts);
	}

	/**
	 * Reduce this partition so that all the parts in the resulting partition are independent.
	 */
	public Partition reduce() {
		if (d_reduced) {
			return this;
		}
		
		UndirectedGraph<Treatment, Part> graph = buildGraph(d_parts);
		if (graph == null) {
			return this;
		}
		
		// Start at an arbitrary part connected to the least vertex.
		// If we don't start at the least vertex, the result might vary for cycles that reduce to a point.
		Treatment v0 = CompareUtil.findLeast(graph.getVertices(), TreatmentComparator.INSTANCE);
		Part p = graph.getIncidentEdges(v0).iterator().next(); // Starting part
		Pair<Treatment> treatments = new Pair<Treatment>(v0, otherTreatment(p, v0));
		Set<Part> visited = new HashSet<Part>(); // Visited parts
		
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
		
		Partition partition = new Partition(reduced);
		partition.d_reduced = true;
		return partition;
	}
	
	public List<Treatment> asCycle() {
		UndirectedGraph<Treatment, Part> graph = buildGraph(d_parts);
		
		List<Treatment> cycle = new ArrayList<Treatment>(d_parts.size() + 1);
		Part p0 = graph.getEdges().iterator().next();
		Pair<Treatment> treatments = new Pair<Treatment>(p0.getTreatments());
		cycle.addAll(treatments);
		for (int i = 1; i < d_parts.size(); ++i) {
			Part pNext = nextPart(graph, cycle.get(i - 1), cycle.get(i));
			cycle.add(otherTreatment(pNext, cycle.get(i)));
		}
		
		return cycle;
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
		visited.add(p0);
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
	 * Determine whether the given set of parts forms a valid partition.
	 */
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
			return p1.getTreatments().equals(p2.getTreatments()) && p1.getTreatments().size() == 2;
		}
		
		// now try to assemble the parts into a cycle
		UndirectedGraph<Treatment, Part> graph = buildGraph(parts);
		return graph == null ? false : GraphUtil.isSimpleCycle(graph);
	}

	/**
	 * Build a graph in which the given parts are the edges.
	 */
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
