/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2010 Gert van Valkenhoef.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

	/**
	 * Calculate the log odds-ratio of c/m compared to a/n (baseline).
	 */
	public static EstimateWithPrecision logOddsRatio(
			int a, int n, int c, int m, boolean corrected) {
		DichotomousDescriptives desc = new DichotomousDescriptives(corrected);
		return new EstimateWithPrecision(
			desc.logOddsRatio(a, n, c, m),
			desc.logOddsRatioError(a, n, c, m));
	}

	/**
	 * Calculate the mean difference of m1 +/- s1 (n1) compared to
	 * m0 +/- s0 (n0) (baseline).
	 */
	public static EstimateWithPrecision meanDifference(
			double m0, double s0, double n0,
			double m1, double s1, double n1) {
		double md = m1 - m0;
		double se = Math.sqrt((s0 * s0) / n0 + (s1 * s1) / n1);
		return new EstimateWithPrecision(md, se);
	}
}
