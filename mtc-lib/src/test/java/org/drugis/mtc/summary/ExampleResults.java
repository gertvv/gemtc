package org.drugis.mtc.summary;

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

public class ExampleResults implements MCMCResults {
	private class MyParameter implements Parameter {
		private String d_name;
		public MyParameter(String name) { d_name = name; }
		public String getName() { return d_name; }
	}
	
	private static final int N_SAMPLES = 500;
	private static final int N_PARAMS = 3;
	private static final int N_CHAINS = 2;
	private double[][][] d_samples = new double[N_CHAINS][N_PARAMS][N_SAMPLES];
	private Parameter[] d_parameters = new Parameter[] {
			new MyParameter("x"), new MyParameter("y"), new MyParameter("s")
	};
	private boolean d_available = false;
	private List<MCMCResultsListener> d_listeners = new ArrayList<MCMCResultsListener>();
	
	public ExampleResults() throws IOException {
		InputStream is = ExampleResults.class.getResourceAsStream("samples.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		reader.readLine(); // skip the first line (column names).
		for (int i = 0; reader.ready(); ++i) {
			String line = reader.readLine();
			StringTokenizer tok = new StringTokenizer(line, ",");
			tok.nextToken(); // skip the first column (IDs)
			for (int j = 0; tok.hasMoreTokens(); ++j) {
				d_samples[i / N_SAMPLES][j][i % N_SAMPLES] = Double.parseDouble(tok.nextToken());
			}
		}
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
	
	public void clear() {
		d_samples = null;
	}
}
