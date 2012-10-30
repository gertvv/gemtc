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

import edu.uci.ics.jung.graph.util.Pair;
import gov.lanl.yadas.ArgumentMaker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.NetworkParameter;
import org.drugis.mtc.parameterization.Parameterization;

/**
 * ArgumentMaker for treatment relative effects.
 * delta_ijk ~ N(d_jk, sigma) ; d_jk = f(B, W) ;
 * B basic parameters, W inconsistency factors.
 */
public class RelativeEffectArgumentMaker implements ArgumentMaker {
	List<Pair<Treatment>> d_delta;
	private final Parameterization d_pmtz;
	private RealMatrix d_matrix;
	private final int d_bIdx;
	private final int d_wIdx;
	
	/**
	 * @param pmtz Parameterization of the network.
	 * @param study The study to calculate relative effects of.
	 * @param bIdx The index of the basic parameter values in the input data.
	 * @param wIdx The index of the inconsistency parameter values in the input
	 * data (may be -1, in which case either there are no inconsistency
	 * parameters, or the inconsistency parameter values are assumed to be
	 * appended to the array of basic parameter values).
	 */
	public RelativeEffectArgumentMaker(Parameterization pmtz, Study study, int bIdx, int wIdx) {
		d_pmtz = pmtz;
		d_bIdx = bIdx;
		d_wIdx = wIdx;
		
		d_delta = new ArrayList<Pair<Treatment>>(study.getTreatments().size() - 1);
		for (List<Pair<Treatment>> list : pmtz.parameterizeStudy(study)) {
			d_delta.addAll(list);
		}
		initializeMatrix();
	}
	
	private void initializeMatrix() {
		List<NetworkParameter> params = d_pmtz.getParameters();
		d_matrix = new Array2DRowRealMatrix(d_delta.size(), params.size());
		for (int i = 0; i < d_delta.size(); ++i) {
			Map<NetworkParameter, Integer> map = d_pmtz.parameterize(d_delta.get(i).getFirst(), d_delta.get(i).getSecond());
			for (int j = 0; j < params.size(); ++j) {
				Integer val = map.get(params.get(j));
				if (val != null) {
					d_matrix.setEntry(i, j, val);
				}
			}
		}
	}

	/**
	 * Calculate "the argument": an array of values for the study's relative effects.
	 * data[bIdx] should contain values of the basic parameters 
	 * data[wIdx] should contain values of the inconsistency factors
	 */
	public double[] getArgument(double[][] data) {
		double[] x = data[d_bIdx];
		if (d_wIdx >= 0) {
			x = new double[data[d_bIdx].length + data[d_wIdx].length];
			System.arraycopy(data[d_bIdx], 0, x, 0, data[d_bIdx].length);
			System.arraycopy(data[d_wIdx], 0, x, data[d_bIdx].length, data[d_wIdx].length);
		}
		return d_matrix.operate(x);
	}
}