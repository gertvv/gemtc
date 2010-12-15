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
