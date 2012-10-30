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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.StringUtils;
import org.drugis.common.CollectionHelper;
import org.drugis.mtc.model.Treatment;

/**
 * A parameter that represents a potential "loop inconsistency" in the network.
 */
public class InconsistencyParameter implements NetworkParameter, Comparable<InconsistencyParameter> {
	private final List<Treatment> d_cycle;
	private static final Transformer<Treatment, String> s_idTransformer = new Transformer<Treatment, String>() {
		public String transform(Treatment input) {
			return input.getId();
		}
	};

	/**
	 * Create an inconsistency parameter for the given (simple) cycle.
	 * @param cycle A cycle (v_1, ..., v_n) in which v_1 = v_n and v_1, ..., v_{n-1} are unique.
	 */
	public InconsistencyParameter(List<Treatment> cycle) {
		d_cycle = cycle;		
	}

	public String getName() {
		return "w." + StringUtils.join(CollectionHelper.transform(d_cycle.subList(0, d_cycle.size() - 1), s_idTransformer), ".");
	}
	
	public List<Treatment> getCycle() {
		return Collections.unmodifiableList(d_cycle);
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof InconsistencyParameter) {
			InconsistencyParameter other = (InconsistencyParameter) obj;
			return d_cycle.equals(other.d_cycle);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return d_cycle.hashCode();
	}

	public int compareTo(InconsistencyParameter other) {
		return compare(d_cycle, other.d_cycle, TreatmentComparator.INSTANCE);
	}

	/**
	 * Compare two lists by pair-wise comparison of their elements.
	 * All other things being equal, the shorter list is considered less than the longer list.
	 * The lists are compared lexicographically, thus if l1[0] > l2[0], l1 > l2 etc.
	 * @param <E> Element type.
	 * @param l1 First list.
	 * @param l2 Second list.
	 * @param cmp Element comparator.
	 * @return An integer < 0 if l1 < l2, > 0 if l1 > l2 and == 0 if l1 == l2.
	 */
	private <E> int compare(List<? extends E> l1, List<? extends E> l2, Comparator<? super E> cmp) {
		int n = Math.min(l1.size(), l2.size());
		for (int i = 0; i < n; ++i) {
			int c = cmp.compare(l1.get(i), l2.get(i));
			if (c != 0) {
				return c; 
			}
		}
		return l1.size() - l2.size();
	}
}
