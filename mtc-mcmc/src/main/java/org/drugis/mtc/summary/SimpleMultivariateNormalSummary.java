package org.drugis.mtc.summary;

import org.drugis.common.beans.AbstractObservable;

public class SimpleMultivariateNormalSummary extends AbstractObservable implements MultivariateNormalSummary {

	private final double[] d_mu;
	private final double[][] d_sigma;

	public SimpleMultivariateNormalSummary(double[] mu, double[][] sigma) {
		d_mu = mu;
		d_sigma = sigma;
	}

	@Override
	public double[][] getCovarianceMatrix() {
		return d_sigma;
	}

	@Override
	public boolean getDefined() {
		return true;
	}

	@Override
	public double[] getMeanVector() {
		return d_mu;
	}
}
