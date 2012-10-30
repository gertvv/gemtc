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

import org.drugis.mtc.model.Treatment;

/**
 * Represents a relative effect parameter that is 'basic' to the parameterization. 
 */
public class BasicParameter implements NetworkParameter, Comparable<BasicParameter> {
	private final Treatment d_base;
	private final Treatment d_subj;

	/**
	 * Basic parameter representing the effect of 'subj' as compared to 'base'.
	 * @param base The baseline (comparator) treatment.
	 * @param subj The subject of the comparison.
	 */
	public BasicParameter(Treatment base, Treatment subj) {
		d_base = base;
		d_subj = subj;	
	}

	public String getName() {
		return "d." + d_base.getId() + "." + d_subj.getId();
	}
	
	public Treatment getBaseline() {
		return d_base;
	}
	
	public Treatment getSubject() {
		return d_subj;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof BasicParameter) {
			BasicParameter other = (BasicParameter) o;
			return d_base.equals(other.d_base) && d_subj.equals(other.d_subj);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return 31 * d_base.hashCode() + d_subj.hashCode();
	}

	public int compareTo(BasicParameter other) {
		int c1 = TreatmentComparator.INSTANCE.compare(d_base, other.d_base);
		return c1 == 0 ? TreatmentComparator.INSTANCE.compare(d_subj, other.d_subj) : c1;
	}
}
