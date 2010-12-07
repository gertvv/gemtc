package org.drugis.mtc.util;

public class Statistics {
	private Statistics() {} // Not instantiable

	/**
	 * Calculate the logit of $p \in [0,1]$.
	 */
	public static double logit(double p) {
		return Math.log(p) - Math.log(1 - p);
	}

	/**
	 * Calculate the inverse logit of $x \in (-\Infty, +\Infty)$.
	 */
	public static double ilogit(double x) {
		return 1.0 / (1 + Math.exp(-x));
	}

	public static EstimateWithPrecision logOddsRatio(
			int a, int n, int c, int m, boolean corrected) {
		DichotomousDescriptives desc = new DichotomousDescriptives(corrected);
		return new EstimateWithPrecision(
			desc.logOddsRatio(a, n, c, m),
			desc.logOddsRatioError(a, n, c, m));
	}
}
