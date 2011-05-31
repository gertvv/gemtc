/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
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

package org.drugis.mtc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsListener;
import org.drugis.mtc.Parameter;

public class WindowResults implements MCMCResults {
	private final MCMCResults d_nested;
	private final int d_start, d_end;
	private List<MCMCResultsListener> d_listeners = new ArrayList<MCMCResultsListener>();
	
	/**
	 * Create a window on the nested results, that only shows the first nSamples.
	 * @param nested
	 * @param nSamples
	 */
	public WindowResults(MCMCResults nested, int start, int end) {
		d_nested = nested;
		d_start = start;
		d_end = end;
	}

	public void addResultsListener(MCMCResultsListener l) {
		d_listeners.add(l);
	}

	public int findParameter(Parameter p) {
		return d_nested.findParameter(p);
	}

	public int getNumberOfChains() {
		return d_nested.getNumberOfChains();
	}

	public int getNumberOfSamples() {
		return d_end - d_start;
	}

	public Parameter[] getParameters() {
		return d_nested.getParameters();
	}

	public double getSample(int p, int c, int i) {
		if (i >= getNumberOfSamples()) {
			throw new IndexOutOfBoundsException("Index " + i + " out of bounds: " + getNumberOfSamples());
		}
		return d_nested.getSample(p, c, i + d_start);
	}

	public void removeResultsListener(MCMCResultsListener l) {
		d_listeners.remove(l);
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}
}
