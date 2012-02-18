/*
 * This file is part of the GeMTC software for MTC model generation and
 * analysis. GeMTC is distributed from http://drugis.org/gemtc.
 * Copyright (C) 2009-2012 Gert van Valkenhoef.
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

package org.drugis.mtc.yadas;

import java.util.Map;
import java.util.Map.Entry;

import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.Parameter;

public class Derivation {
	private final Map<? extends Parameter, Integer> d_pmtz;
	
	public Derivation(Map<? extends Parameter, Integer> pmtz) {
		assert(!pmtz.isEmpty());
		d_pmtz = pmtz;	
	}
	

	public double[] calculate(MCMCResults results, int c) {
		double[] result = new double[results.getNumberOfSamples()];
		for (int i = 0; i < result.length; ++i) {
			result[i] = calculate(results, c, i);
		}
		return result;
	}
	
	public double calculate(MCMCResults results, int c, int i) {
		double val = 0.0;
		for (Entry<? extends Parameter, Integer> e : d_pmtz.entrySet()) {
			val += e.getValue() * results.getSample(results.findParameter(e.getKey()), c, i);
		}
		return val;
	}
}
