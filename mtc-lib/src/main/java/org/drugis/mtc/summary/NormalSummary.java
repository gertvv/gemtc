package org.drugis.mtc.summary;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.drugis.common.beans.AbstractObservable;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;
import org.drugis.mtc.Parameter;

/**
 * Summary of data assumed to be normally distributed; i.e., that have mean and standard deviation.
 */
public class NormalSummary extends AbstractObservable implements MCMCResultsListener {
	public static final String PROPERTY_MEAN = "mean";
	public static final String PROPERTY_STANDARD_DEVIATION = "standardDeviation";
	
	private static final Mean s_mean = new Mean(); 
	private static final StandardDeviation s_stdev = new StandardDeviation();
	
	private final MCMCResults d_results;
	private final Parameter d_parameter;
	private double d_mean = 0.0;
	private double d_stdev = 0.0;

	public NormalSummary(MCMCResults results, Parameter parameter) {
		d_results = results;
		d_parameter = parameter;
		calculateResults();
		results.addResultsListener(this);
	}

	public void resultsEvent(MCMCResultsEvent event) {
		calculateResults();
	}
	
	public boolean isDefined() {
		return d_results.getNumberOfSamples() >= 4;
	}
	
	public double getMean() {
		return d_mean;
	}
		
	public double getStandardDeviation() {
		return d_stdev;
	}
	
	private synchronized void calculateResults() {
		if (!isDefined()) return;
		double[] samples = getSamples();
		d_mean = s_mean.evaluate(samples);
		d_stdev = s_stdev.evaluate(samples);
		firePropertyChange(PROPERTY_MEAN, null, d_mean);
		firePropertyChange(PROPERTY_STANDARD_DEVIATION, null, d_stdev);
	}

	private double[] getSamples() {
		int n = d_results.getNumberOfSamples() / 2;
		int c = d_results.getNumberOfChains();
		double[] samples = new double[n * c];
		int p = d_results.findParameter(d_parameter);
		for (int i = 0; i < c; ++i) {
			System.arraycopy(d_results.getSamples(p, i), n, samples, i * n, n);
		}
		return samples;
	}
}
