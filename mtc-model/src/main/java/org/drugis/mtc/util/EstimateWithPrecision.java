package org.drugis.mtc.util;

/**
 * Represents a point estimate with associated standard error.
 */
public class EstimateWithPrecision {
	private final double d_pe;
	private final double d_se;

	/**
	 * @param pe The point estimate.
	 * @param se The standard error for the point estimate.
	 */
	public EstimateWithPrecision(double pe, double se) {
		d_pe = pe;
		d_se = se;
	}

	public double getPointEstimate() {
		return d_pe;
	}

	public double getStandardError() {
		return d_se;
	}
}
