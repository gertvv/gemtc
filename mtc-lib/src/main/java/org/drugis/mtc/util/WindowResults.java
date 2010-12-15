/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2010 Gert van Valkenhoef.
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
	private int d_nSamples;
	private final Parameter[] d_parameters;
	private final int d_nChains;
	private double[][][] d_samples;
	private List<MCMCResultsListener> d_listeners = new ArrayList<MCMCResultsListener>();
	
	/**
	 * Create a window on the nested results, that only shows the first nSamples.
	 * @param nested
	 * @param nSamples
	 */
	public WindowResults(MCMCResults nested, int start, int end) {
		d_nSamples = end - start;
		d_parameters = nested.getParameters();
		d_nChains = nested.getNumberOfChains();
		d_samples = new double[d_nChains][d_parameters.length][d_nSamples];
		for(int c = 0; c < d_nChains; ++c) {
			for(Parameter p: d_parameters) {
				int par = findParameter(p);
				System.arraycopy(nested.getSamples(par, c), start, d_samples[c][par], 0, d_nSamples);
			}
		}
	}

	public void addResultsListener(MCMCResultsListener l) {
		d_listeners.add(l);
	}

	public int findParameter(Parameter p) {
		return Arrays.asList(d_parameters).indexOf(p);
	}

	public int getNumberOfChains() {
		return d_nChains;
	}

	public int getNumberOfSamples() {
		return d_nSamples;
	}

	public Parameter[] getParameters() {
		return d_parameters;
	}

	public double getSample(int p, int c, int i) {
		return d_samples[c][p][i];
	}

	public double[] getSamples(int p, int c) {
		return d_samples[c][p];
	}

	public void removeResultsListener(MCMCResultsListener l) {
		d_listeners.remove(l);
	}

	public void clear() {
		d_samples = null;
		d_nSamples = 0;
	}
}
