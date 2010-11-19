package org.drugis.mtc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsListener;
import org.drugis.mtc.Parameter;

public class WindowResults implements MCMCResults {
	private final int d_nSamples;
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
}
