package org.drugis.mtc.parameterization;

import java.util.Comparator;

/**
 * Defines an order on NetworkParameter, where BasicParameter < ...
 */
public class NetworkParameterComparator implements Comparator<NetworkParameter> {
	TreatmentComparator s_tc = new TreatmentComparator();
	
	public int compare(NetworkParameter p1, NetworkParameter p2) {
		BasicParameter b1 = (BasicParameter) p1;
		BasicParameter b2 = (BasicParameter) p2;
		int c1 = s_tc.compare(b1.getBaseline(), b2.getBaseline());
		return c1 == 0 ? s_tc.compare(b1.getSubject(), b2.getSubject()) : c1;
	}
}
