package org.drugis.mtc.parameterization;

import java.util.Collection;
import java.util.Comparator;

import org.drugis.mtc.model.Treatment;

public class TreatmentComparator implements Comparator<Treatment> {
	public static final TreatmentComparator INSTANCE = new TreatmentComparator();

	@Override
	public int compare(Treatment o1, Treatment o2) {
		return o1.getId().compareTo(o2.getId());
	}
	
	/**
	 * Find the "least" of the given treatments
	 * @param treatments
	 * @return
	 */
	public static Treatment findLeast(Collection<Treatment> treatments) {
		Treatment least = null;
		for (Treatment t : treatments) {
			if (least == null || TreatmentComparator.INSTANCE.compare(t, least) < 0) {
				least = t;
			}
		}
		return least;
	}
}
