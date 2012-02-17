package org.drugis.mtc.parameterization;

import java.util.Comparator;

/**
 * Defines an order on NetworkParameter, where BasicParameter < ...
 */
public class NetworkParameterComparator implements Comparator<NetworkParameter> {
	public static final NetworkParameterComparator INSTANCE = new NetworkParameterComparator();
	
	public int compare(NetworkParameter p1, NetworkParameter p2) {
		if (compareType(p1, p2) != 0) {
			return compareType(p1, p2);
		}
		
		if (p1 instanceof BasicParameter) {
			return ((BasicParameter) p1).compareTo((BasicParameter) p2);
		} else if (p2 instanceof SplitParameter) {
			return ((SplitParameter) p1).compareTo((SplitParameter) p2);
		} else {
			return ((InconsistencyParameter) p1).compareTo((InconsistencyParameter) p2);
		}
	}
	
	private int compareType(NetworkParameter p1, NetworkParameter p2) {
		int type1 = p1 instanceof BasicParameter ? 0 : 1;
		int type2 = p2 instanceof BasicParameter ? 0 : 1;
		return type1 - type2;
	}
}
