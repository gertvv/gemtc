package org.drugis.mtc.summary;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.correlation.StorelessCovariance;
import org.drugis.common.beans.AbstractObservable;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;
import org.drugis.mtc.Parameter;

public class MCMCMultivariateNormalSummary extends AbstractObservable implements MCMCResultsListener, Summary, MultivariateNormalSummary {

	private static final Mean s_mean = new Mean();

	private final MCMCResults d_results;
	private final Parameter[] d_parameters;
	private double[][] d_covMatrix;
	private double[] d_means;
	private boolean d_isDefined = false;

	public MCMCMultivariateNormalSummary(MCMCResults results, Parameter[] parameters) {
		d_results = results;
		d_parameters = parameters;
		d_means = new double[getParameters().length];
		d_covMatrix = new double[getParameters().length][getParameters().length]; 
		calculateResults();
		d_results.addResultsListener(this);
	}


	public Parameter[] getParameters() {
		return d_parameters;
	}

	/* (non-Javadoc)
	 * @see org.drugis.mtc.summary.iMultivariateNormalSummaryPod#getDefined()
	 */
	@Override
	public boolean getDefined() {
		return d_isDefined;
	}
	@Override
	public void resultsEvent(MCMCResultsEvent event) {
		calculateResults();
	}
	
	/* (non-Javadoc)
	 * @see org.drugis.mtc.summary.iMultivariateNormalSummaryPod#getMeanVector()
	 */
	public double[] getMeanVector() {
		return d_means;
	}
	
	/* (non-Javadoc)
	 * @see org.drugis.mtc.summary.iMultivariateNormalSummaryPod#getCovarianceMatrix()
	 */
	public double[][] getCovarianceMatrix() {
		return d_covMatrix;	
	}

	private boolean isReady() {
		return d_results.getNumberOfSamples() >= 4;
	}

	private void calculateResults() {
		if (!isReady()) {
			return;
		}
		List<List<Double>> sampleCache = new ArrayList<List<Double>>();
		for (int i = 0; i < getParameters().length; ++i) {
			List<Double> samples = SummaryUtil.getAllChainsLastHalfSamples(d_results, getParameters()[i]);
			sampleCache.add(samples);
			d_means[i] = SummaryUtil.evaluate(s_mean, samples);
		}
		StorelessCovariance cov = new StorelessCovariance(getParameters().length);
		double[] rowData = new double[getParameters().length];
		for (int row = 0; row < sampleCache.get(0).size(); ++row) {
			for (int col = 0; col < getParameters().length; ++col) {
				rowData[col] = sampleCache.get(col).get(row);
			}
			cov.increment(rowData);
		}
		d_covMatrix = cov.getData();
		boolean wasDefined = d_isDefined;
		d_isDefined = true;
		firePropertyChange(PROPERTY_DEFINED, wasDefined, d_isDefined);
		firePropertyChange(PROPERTY_MEAN_VECTOR, null, d_means);
		firePropertyChange(PROPERTY_COVARIANCE_MATRIX, null, d_covMatrix);
	}
}
