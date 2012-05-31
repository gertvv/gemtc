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

package org.drugis.mtc.summary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.drugis.common.beans.AbstractObservable;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;

public class RankProbabilitySummary extends AbstractObservable implements MCMCResultsListener, Summary {
	public static final String PROPERTY_VALUE = "value";
	private List<Treatment> d_treatments;
	private MCMCResults d_results;
	private int d_n;
	private double[][] d_rankProbability;
	private boolean d_ready = false;

	public RankProbabilitySummary(MCMCResults results, List<Treatment> treatments) {
		d_results = results;
		d_results.addResultsListener(this);
		d_treatments = treatments;
		d_n = treatments.size();
		calculate();
	}	
	
	public RankProbabilitySummary(double[][] rankProbabilityMatrix, List<Treatment> treatments) {
		d_rankProbability = rankProbabilityMatrix;
		d_treatments = treatments;
		d_n = treatments.size();
		d_ready = true;
	}

	public void resultsEvent(MCMCResultsEvent event) {
		calculate();
		firePropertyChange(PROPERTY_DEFINED, null, getDefined());
		firePropertyChange(PROPERTY_VALUE, null, this);
	}
	
	public List<Treatment> getTreatments() {
		return Collections.unmodifiableList(d_treatments);
	}

	public double getValue(Treatment t, int rank) {
		if (!d_ready) {
			return 0.0;
		}
		int rIdx = d_n - rank;
		int tIdx = d_treatments.indexOf(t);
		return d_rankProbability[tIdx][rIdx];
	}

	private synchronized void calculate() {
		d_ready =  d_results.getNumberOfSamples() > 0;
		if (!d_ready) {
			return;
		}
		Treatment base = d_treatments.get(0);
		List<List<Double>> samples = new ArrayList<List<Double>>();
		for (int i = 1; i < d_n; ++i ) {
			samples.add(SummaryUtil.getAllChainsLastHalfSamples(d_results, new BasicParameter(base, d_treatments.get(i))));
		}

		int[][] rankCount = new int[d_n][d_n];
		final int nSamples = samples.get(0).size();
		for (int i = 0; i < nSamples; ++i) {
			double[] data = new double[d_n];
			for (int j = 1; j < d_n; ++j) {
				data[j] = samples.get(j - 1).get(i);
			}
			int[] ranks = RankCounter.rank(data);
			for (int j = 0; j < d_n; ++j) {
				rankCount[j][ranks[j] - 1] += 1;
			}
		}
		
		d_rankProbability = new double[d_n][d_n];
		for (int i = 0; i < d_n; ++i) {
			for (int j = 0; j < d_n; ++j) {
				d_rankProbability[i][j] = ((double)rankCount[i][j]) / ((double)nSamples);
			}
		}
	}

	public boolean getDefined() {
		return d_ready; 
	}
}
