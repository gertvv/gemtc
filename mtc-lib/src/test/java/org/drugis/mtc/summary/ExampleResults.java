package org.drugis.mtc.summary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;
import org.drugis.mtc.Parameter;

public class ExampleResults implements MCMCResults {
	private class MyParameter implements Parameter {
		private String d_name;
		public MyParameter(String name) { d_name = name; }
		public String getName() { return d_name; }
	}
	
	private static final int N_SAMPLES = 100;
	private static final int N_PARAMS = 5;
	private static final int N_CHAINS = 2;
	private double[][][] d_samples = new double[N_CHAINS][N_PARAMS][N_SAMPLES];
	private Parameter[] d_parameters = new Parameter[] {
			new MyParameter("A"), new MyParameter("B"), new MyParameter("C"), 
			new MyParameter("D"), new MyParameter("E")
	};
	private boolean d_available = false;
	private List<MCMCResultsListener> d_listeners = new ArrayList<MCMCResultsListener>();
	
	public ExampleResults() {
		// FIXME: read samples
	}

	public Parameter[] getParameters() {
		return d_parameters;
	}

	public int findParameter(Parameter p) {
		return Arrays.asList(d_parameters).indexOf(p);
	}

	public int getNumberOfChains() {
		return N_CHAINS;
	}

	public int getNumberOfSamples() {
		return d_available ? N_SAMPLES : 0;
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
}
