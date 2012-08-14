package org.drugis.mtc.gui;

import java.util.Collection;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;

public class Util {

	public static <E> BidiMap<E, E> identityMap(final Collection<? extends E> objs) {
		BidiMap<E, E> map = new DualHashBidiMap<E, E>();
		for (E e : objs) {
			map.put(e, e);
		}
		return map;
	}

}
