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

import java.util.Arrays;
import java.util.List;

import org.drugis.mtc.model.Treatment;

/**
 * ArgumentMaker for individual treatment means within studies.
 * theta_i,k = mu_i + delta_i,b(i),k
 */
public class ThetaArgumentMaker implements ArgumentMaker {
	private final List<Treatment> d_treatments;
	private final List<List<Pair<Treatment>>> d_studyPmtz;
	private final int d_muIdx;
	private final int d_deltaIdx;
	private int[] d_parent;
	private int[] d_treatmentDeltaIndex;

	/**
	 * @param treatments The list of treatments to generate treatment effects for.
	 * @param studyPmtz The study parameterization: (baseline, subject) pairs, forming a tree.
	 * @param muIdx The index where the baseline effects (mu) are expected.
	 * @param deltaIdx The index where the relative effects (delta) are expected.
	 */
	public ThetaArgumentMaker(List<Treatment> treatments, List<List<Pair<Treatment>>> studyPmtz, int muIdx, int deltaIdx) {
		d_treatments = treatments;
		d_studyPmtz = studyPmtz;
		d_muIdx = muIdx;
		d_deltaIdx = deltaIdx;
		
		d_parent = new int[d_treatments.size()]; // Lookup map treatment --> baseline
		Arrays.fill(d_parent, -1);
		d_treatmentDeltaIndex = new int[d_treatments.size()]; // Lookup map treatment --> delta
		Arrays.fill(d_treatmentDeltaIndex, -1);
		int dIdx = 0;
		for (int i = 0; i < d_studyPmtz.size(); ++i) {
			for (Pair<Treatment> pair : d_studyPmtz.get(i)) {
				int bIdx = d_treatments.indexOf(pair.getFirst());
				int tIdx = d_treatments.indexOf(pair.getSecond());
				d_parent[tIdx] = bIdx;
				d_treatmentDeltaIndex[tIdx] = dIdx;
				++dIdx;
			}
		}
		
		int cnt = 0;
		for (int i = 0; i < d_parent.length; ++i) {
			if (d_parent[i] == -1) {
				++cnt;
			}
		}
		if (cnt != 1) {
			throw new IllegalArgumentException("The studyParameterization should have a tree structure.");
		}
	}

	/**
	 * Calculate "the argument": an array of treatment effects, one for each study-arm.
	 * data[muIdx] should contain a single study baseline mean
	 * data[deltaIdx] should contain relative effects, in the order of occurrence in studyPmtz
	 */
	public double[] getArgument(double[][] data) {
		double[] rval = new double[d_treatments.size()];
		for (int i = 0; i < rval.length; ++i) {
			rval[i] = theta(i, data);
		}
		return rval;
	}

	protected double theta(int tIdx, double[][] data) {
		double delta = 0.0;
		while (d_parent[tIdx] != -1) {
			int dIdx = d_treatmentDeltaIndex[tIdx];
			delta += data[d_deltaIdx][dIdx];
			tIdx = d_parent[tIdx];
		}
		
		return data[d_muIdx][0] + delta;
	}
}