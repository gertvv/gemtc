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

public class SplitParameter implements NetworkParameter, Comparable<SplitParameter> {
	
	private final Treatment d_base;
	private final Treatment d_subj;
	private final boolean d_direct;

	public SplitParameter(Treatment base, Treatment subj, boolean direct) {
		d_base = base;
		d_subj = subj;
		d_direct = direct;
	}

	public String getName() {
		return "d." + d_base.getId() + "." + d_subj.getId() + "." + (d_direct ? "dir" : "ind");
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SplitParameter) {
			SplitParameter other = (SplitParameter) obj;
			return d_base.equals(other.d_base) && d_subj.equals(other.d_subj) && d_direct == other.d_direct;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return 31 * d_base.hashCode() + d_subj.hashCode() + (d_direct ? 1 : 0);
	}

	public int compareTo(SplitParameter other) {
		TreatmentComparator tc = TreatmentComparator.INSTANCE;
		if (tc.compare(d_base, other.d_base) != 0) {
			return tc.compare(d_base, other.d_base);
		}
		if (tc.compare(d_subj, other.d_subj) != 0) {
			return tc.compare(d_subj, other.d_subj);
		}
		if (d_direct) {
			return other.d_direct ? 0 : -1;
		} else {
			return other.d_direct ? 1 : 0;
		}
	}

	public Treatment getBaseline() {
		return d_base;
	}
	
	public Treatment getSubject() {
		return d_subj;
	}
	
	public boolean isDirect() { 
		return d_direct;
	}
}
