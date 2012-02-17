package org.drugis.mtc.parameterization;

import java.util.Collection;
import java.util.Comparator;

public class CompareUtil {
	/**
	 * Find the "least" of the given collection
	 */
	public static <E> E findLeast(Collection<E> coll, Comparator<? super E> cmp) {
		E least = null;
		for (E t : coll) {
			if (least == null || cmp.compare(t, least) < 0) {
				least = t;
			}
		}
		return least;
	}
}
