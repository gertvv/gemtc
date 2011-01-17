/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
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

package org.drugis.mtc.summary;

import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.Parameter;

public class SummaryUtil {

	/**
	 * Get the last 1/2 of the samples from each chain in results, for parameter.
	 */
	public static double[] getAllChainsLastHalfSamples(MCMCResults results, Parameter parameter) {
		int n = results.getNumberOfSamples() / 2;
		int c = results.getNumberOfChains();
		double[] samples = new double[n * c];
		int p = results.findParameter(parameter);
		for (int i = 0; i < c; ++i) {
			System.arraycopy(results.getSamples(p, i), n, samples, i * n, n);
		}
		return samples;
	}
	
	/**
	 * Get the last 1/2 of the samples from one chain in results, for parameter.
	 */
	public static double[] getOneChainLastHalfSamples(MCMCResults results, Parameter parameter, int c) {
		int n = results.getNumberOfSamples() / 2;
		double[] samples = new double[n];
		int p = results.findParameter(parameter);
		System.arraycopy(results.getSamples(p, c), n, samples, 0, n);
		return samples;
	}
}
