package org.drugis.mtc.util;

import java.sql.NClob;
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
	private boolean d_available = false;
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
//		double [] temp = new double[d_nSamples];
		for(int c = 0; c < d_nChains; ++c) {
			for(Parameter p: d_parameters) {
				int par = findParameter(p);
				System.arraycopy(nested.getSamples(par, c), start, d_samples[c][par], 0, d_nSamples);
			}
		}
	}

	public void addResultsListener(MCMCResultsListener l) {
		// TODO Auto-generated method stub
		
	}

	public int findParameter(Parameter p) {
		return Arrays.asList(d_parameters).indexOf(p);
	}

	public int getNumberOfChains() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getNumberOfSamples() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Parameter[] getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	public double getSample(int p, int c, int i) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double[] getSamples(int p, int c) {
		return d_samples[c][p];
	}

	public void removeResultsListener(MCMCResultsListener l) {
		// TODO Auto-generated method stub
		
	}
}
