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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.search.SearchProblem;

import edu.uci.ics.jung.algorithms.transformation.FoldingTransformerFixed.FoldedEdge;
import edu.uci.ics.jung.graph.UndirectedGraph;

/**
 * Defines the SearchProblem that is to be solved in order to correctly assign baselines for an inconsistency model.
 */
public class InconsistencyBaselineSearchProblem implements SearchProblem<Map<Study, Treatment>>{
	private final Collection<Study> d_studies;
	private final UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> d_cGraph;
	private final Map<Partition, Set<List<Treatment>>> d_cycleClasses;

	/**
	 * Search problem for finding a baseline assignment that covers all edges.
	 * @param studies List of studies.
	 * @param cGraph Comparison graph.
	 */
	public InconsistencyBaselineSearchProblem(Collection<Study> studies, UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph) {
		this(studies, cGraph, null);
	}
	
	public InconsistencyBaselineSearchProblem(Collection<Study> studies, UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph, Map<Partition, Set<List<Treatment>>> cycleClasses) {
		d_studies = studies;
		d_cGraph = cGraph;
		d_cycleClasses = cycleClasses;
	}

	public Map<Study, Treatment> getInitialState() {
		Map<Study, Treatment> state = new HashMap<Study, Treatment>();
		for (Study s : d_studies) {
			Set<Treatment> treatments = s.getTreatments();
			if (treatments.size() == 2) {
				state.put(s, CompareUtil.findLeast(treatments, TreatmentComparator.INSTANCE));
			}
		}
		return state;
	}

	public List<Map<Study, Treatment>> getSuccessors(final Map<Study, Treatment> state) {
		// Find the first study without a baseline
		Study study = CollectionUtils.find(d_studies, new Predicate<Study>() {
			public boolean evaluate(Study study) {
				return !state.containsKey(study);
			}
		});
		
		if (study == null) {
			return Collections.emptyList();
		}
		
		// Each treatment generates a successor
		Set<Treatment> treatments = new TreeSet<Treatment>(new TreatmentComparator());
		treatments.addAll(study.getTreatments());
		List<Map<Study, Treatment>> succ = new ArrayList<Map<Study,Treatment>>(treatments.size());
		for (Treatment treatment : treatments) {
			Map<Study, Treatment> map = new HashMap<Study, Treatment>(state);
			map.put(study, treatment);
			succ.add(map);
		}
		
		return succ;
	}

	public boolean isGoal(Map<Study, Treatment> state) {
		if (!state.keySet().containsAll(d_studies)) {
			return false;
		}
		
		// Calculate covered edges
		Set<FoldedEdge<Treatment, Study>> covered = new HashSet<FoldedEdge<Treatment,Study>>();
		for (Entry<Study, Treatment> entry : state.entrySet()) {
			Treatment t0 = entry.getValue();
			for (Treatment t1 : entry.getKey().getTreatments()) {
				FoldedEdge<Treatment, Study> edge = d_cGraph.findEdge(t0, t1);
				if (edge != null) {
					covered.add(edge);
				}
			}
		}
		
		if (d_cycleClasses == null) { // Looking for full baseline cover
			return covered.containsAll(d_cGraph.getEdges());
		}

		// Now check that for each cycle class, all cycles have at least (n-1) edges covered,
		// and if the cycle is potentially inconsistent, that at least one as n edges covered.
		for (Entry<Partition, Set<List<Treatment>>> c : d_cycleClasses.entrySet()) {
			Set<List<Treatment>> cycles = c.getValue();
			if (existsTooManyMissing(cycles, covered)) {
				return false;
			} else if (InconsistencyParameterization.isInconsistencyCycle(c.getKey()) && !existsNoneMissing(cycles, covered)) {
				return false;
			}
		}
		return true;
	}

	private boolean existsNoneMissing(Set<List<Treatment>> cycles, final Set<FoldedEdge<Treatment, Study>> covered) {
		return CollectionUtils.exists(cycles, new Predicate<List<Treatment>>() {
			public boolean evaluate(List<Treatment> cycle) {
				return countMissingEdges(cycle, covered) == 0;
			}
		});
	}

	private boolean existsTooManyMissing(Set<List<Treatment>> cycles, final Set<FoldedEdge<Treatment, Study>> covered) {
		return CollectionUtils.exists(cycles, new Predicate<List<Treatment>>() {
			public boolean evaluate(List<Treatment> cycle) {
				return countMissingEdges(cycle, covered) < -1;
			}
		});
	}
	
	private int countMissingEdges(List<Treatment> cycle, Set<FoldedEdge<Treatment, Study>> covered) {
		int missing = 0;
		for (int i = 1; i < cycle.size(); ++i) {
			FoldedEdge<Treatment, Study> edge = d_cGraph.findEdge(cycle.get(i - 1), cycle.get(i));
			if (!covered.contains(edge)) {
				++missing;
			}
		}
		return missing;
	}
}
