package org.drugis.mtc.util;

import java.util.List;
import java.util.ArrayList;

/**
 * DerSimonian-Laird random effects meta-analysis.
 * Takes a list of point estimates and standard errors, and gives a pooled
 * estimate + SE. Also calculates heterogeneity.
 */
public class DerSimonianLairdPooling {
	private double d_pe;
	private double d_se;
	private double d_q;
	private int d_n;

	public DerSimonianLairdPooling(List<EstimateWithPrecision> estimates) {
		this(getPointEstimates(estimates), getStandardErrors(estimates));
	}

	public DerSimonianLairdPooling(final double[] pe, final double[] se) {
		if (pe.length != se.length) {
			throw new IllegalArgumentException("Input arrays must have equal length");
		}
		if (pe.length == 0) {
			throw new IllegalArgumentException("Input may not be empty");
		}

		d_n = pe.length;

		final double[] weights = calculateWeights(se);
		d_q = calculateQ(weights, pe);

		final double[] adjWeights = calculateAdjustedWeights(weights, se, d_q);
		d_pe = calculatePointEstimate(adjWeights, pe);
		d_se = calculateError(adjWeights);
	}

	/**
	 * Get the pooled estimate.
	 */
	public EstimateWithPrecision getPooled() {
		return new EstimateWithPrecision(d_pe, d_se);
	}

	/**
	 * The heterogeneity Q value (inverse variance).
	 */
	public double getHeterogeneity() {
		return d_q;
	}

	/**
	 * The I^2 heterogeneity test statitistic.
	 */
	public double getHeterogeneityTestStatistic() {
		return Math.max(0, (d_q - (d_n - 1)) / d_q);
	}

	private static double[] calculateWeights(double[] se) {
		double[] weights = new double[se.length];
		for (int i = 0; i < se.length; ++i) {
			weights[i] = 1.0 / (se[i] * se[i]);
		}
		return weights;
	}

	private static double calculateQ(double[] weights, double[] pe) {
		double theta = calculateTheta(weights, pe);
		double sum = 0;
		for (int i = 0; i < weights.length; ++i) {
			double dev = pe[i] - theta;
			sum += weights[i] * dev * dev;
		}
		return sum;
	}

	private static double calculateTheta(double[] weights, double[] pe) {
		double sum = 0;
		for (int i = 0; i < weights.length; ++i) {
			sum += weights[i] * pe[i];
		}
		return sum / sum(weights);
	}

	private static double sum(double[] l) {
		double sum = 0;
		for (double d : l) {
			 sum += d;
		}
		return sum;
	}

	private static double sumOfSquares(double[] l) {
		double sum = 0;
		for (double d : l) {
			 sum += d * d;
		}
		return sum;
	}

	private static double[] calculateAdjustedWeights(double[] weights, double[] se, double q) {
		if (weights.length < 2) {
			return weights;
		}

		final double tauSquared = getTauSquared(weights, q);

		double[] adjusted = new double[weights.length];
		for (int i = 0; i < weights.length; ++i) {
			adjusted[i] = 1.0 / (se[i] * se[i] + tauSquared);
		}
		return adjusted;
	}

	private static double getTauSquared(double[] weights, double q) {
		double k = weights.length;
		double num = q - (k - 1);
		double den = sum(weights) - (sumOfSquares(weights) / sum(weights));
		return Math.max(num / den, 0);
	}
	
	private static double calculatePointEstimate(double[] adjWeights, double[] pe) {
		double sum = 0;
		for (int i = 0; i < adjWeights.length; ++i) {
			sum += adjWeights[i] * pe[i];
		}
		return sum / sum(adjWeights);
	}

	private static double calculateError(double[] adjWeights) {
		return 1.0 / Math.sqrt(sum(adjWeights));
	}

	private static double[] getPointEstimates(List<EstimateWithPrecision> estimates) {
		double[] pe = new double[estimates.size()];
		for (int i = 0; i < pe.length; ++i) {
			pe[i] = estimates.get(i).getPointEstimate();
		}
		return pe;
	}

	private static double[] getStandardErrors(List<EstimateWithPrecision> estimates) {
		double[] se = new double[estimates.size()];
		for (int i = 0; i < se.length; ++i) {
			se[i] = estimates.get(i).getStandardError();
		}
		return se;
	}
}
