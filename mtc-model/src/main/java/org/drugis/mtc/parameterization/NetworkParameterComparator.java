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
