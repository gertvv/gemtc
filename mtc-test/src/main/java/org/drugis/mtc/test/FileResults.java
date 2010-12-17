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

package org.drugis.mtc.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;
import org.drugis.mtc.Parameter;

public class FileResults implements MCMCResults {
	public static class MyParameter implements Parameter {
		private String d_name;
		public MyParameter(String name) { d_name = name; }
		public String getName() { return d_name; }
	}
	
	private final int d_nSamples;
	private final Parameter[] d_parameters;
	private final int d_nChains;
	private double[][][] d_samples;
	private boolean d_available = false;
	private List<MCMCResultsListener> d_listeners = new ArrayList<MCMCResultsListener>();

	public FileResults(InputStream is, Parameter[] parameters, int nChains, int nSamples) throws IOException {
		d_parameters = parameters;
		d_nChains = nChains;
		d_nSamples = nSamples;
		d_samples = new double[d_nChains][d_parameters.length][d_nSamples];
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		reader.readLine(); // skip the first line (column names).
		for (int i = 0; reader.ready(); ++i) {
			String line = reader.readLine();
			StringTokenizer tok = new StringTokenizer(line, ",");
			tok.nextToken(); // skip the first column (IDs)
			for (int j = 0; tok.hasMoreTokens(); ++j) {
				d_samples[i / d_nSamples][j][i % d_nSamples] = Double.parseDouble(tok.nextToken());
			}
		}
		is.close();
	}

	public Parameter[] getParameters() {
		return d_parameters;
	}

	public int findParameter(Parameter p) {
		return Arrays.asList(d_parameters).indexOf(p);
	}

	public int getNumberOfChains() {
		return d_nChains;
	}

	public int getNumberOfSamples() {
		return d_available ? d_nSamples : 0;
	}

	public double getSample(int p, int c, int i) {
		if (!d_available) throw new IndexOutOfBoundsException("No samples available");
		return d_samples[c][p][i];
	}

	public double[] getSamples(int p, int c) {
		if (!d_available) throw new IndexOutOfBoundsException("No samples available");
		return d_samples[c][p];
	}

	public void addResultsListener(MCMCResultsListener l) {
		d_listeners.add(l);
	}

	public void removeResultsListener(MCMCResultsListener l) {
		d_listeners.remove(l);
	}

	public void makeSamplesAvailable() {
		d_available = true;
		for (MCMCResultsListener l : d_listeners) {
			l.resultsEvent(new MCMCResultsEvent(this));
		}
	}

	public void clear() {
		d_samples = null;
	}
}
