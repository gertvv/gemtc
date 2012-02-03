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

	@Override
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

	@Override
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

}
