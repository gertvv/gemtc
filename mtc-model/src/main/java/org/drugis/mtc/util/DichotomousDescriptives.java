package org.drugis.mtc.util;

import static org.drugis.mtc.util.Statistics.logit;

public class DichotomousDescriptives {
	public final double d_correction;

	public DichotomousDescriptives() {
		this(false);
	}

	public DichotomousDescriptives(boolean corrected) {
		d_correction = corrected ? 0.5 : 0.0;
	}

	/**
	 * Calculate the log-odds ratio log(bc/ad). b = (n - a), d = (m - c).
	 * @param a Number of events (control).
	 * @param n Possible number of events (control).
	 * @param c Number of events (intervention).
	 * @param m Possible number of events (intervention).
	 */
	public double logOddsRatio(int a, int n, int c, int m) {
		return logOdds(c, m) - logOdds(a, n);
	}

	/**
	 * Calculate the standard error of the log-odds ratio.
	 */
	public double logOddsRatioError(int a, int n, int c, int m) {
		int b = n - a;
		int d = m - c;
		return Math.sqrt(1.0 / (a + d_correction) + 1.0 / (b + d_correction) +
			1.0 / (c + d_correction) + 1.0 / (d + d_correction));
	}

	/**
	 * Calculate the log-odds of an event log(a) - log(n - a).
	 */
	public double logOdds(int a, int n) {
		return logit(risk(a, n));
	}

	/**
	 * Calculate the risk a/n of an event.
	 */
	public double risk(int a, int n) {
		return (a + d_correction) / (n + 2 * d_correction);
	}
}
