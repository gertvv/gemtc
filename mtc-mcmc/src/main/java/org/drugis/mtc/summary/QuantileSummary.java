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

import java.util.List;
import java.util.Arrays;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.drugis.common.beans.AbstractObservable;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;
import org.drugis.mtc.Parameter;

/**
 * Summary of data based on sample quantiles. A list (p_1, ..., p_n) of 
 * probabilities in [0, 1] is given, and the summary will estimate quantiles
 * that correspond to these probabilities. 
 * Note that the quantiles correspond to "type 6" quantiles in R.
 */
public class QuantileSummary extends AbstractObservable implements MCMCResultsListener, Summary {
	private static final double[] DEFAULT_PROBABILITIES = new double[] {0.025, 0.5, 0.975};
	private final double[] d_probabilities;
	private Parameter d_parameter;
	private MCMCResults d_results;
	private double[] d_quantiles;
	private boolean d_defined = false;
	private boolean d_continuous = false; 

	public QuantileSummary(MCMCResults results, Parameter parameter, double[] probabilities) {
		this.d_probabilities = probabilities;
		d_results = results;
		d_parameter = parameter;
		d_results.addResultsListener(this);
		calculateResults();
	}
	
	/**
	 * Default probabilities (0.025, 0.5, 0.975): median and 95% interval.
	 */
	public QuantileSummary(MCMCResults results, Parameter parameter) {
		this(results, parameter, DEFAULT_PROBABILITIES);
	}

	public void resultsEvent(MCMCResultsEvent event) {
		calculateResults();
	}

	/**
	 * Get the index at which the quantile with probability p is stored.
	 * @return The index, or -1 if p is not a calculated quantile.
	 */
	public int indexOf(double p) {
		for (int i = 0; i < d_probabilities.length; i++ ) {
			if (d_probabilities[i] == p) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Return the probability for which the quantile is stored at index idx.
	 */
	public double getProbability(int idx) {
		return d_probabilities[idx];
	}
	
	/**
	 * Return the quantile stored at index idx.
	 */
	public double getQuantile(int idx) {
		return d_quantiles[idx];
	}
	
	private double calculateQuantile(int i, double[] samples) {
		double p = getProbability(i);
		Percentile q = new Percentile(p * 100.0);
		return q.evaluate(samples);
	}

	@Deprecated
	private double[] getSamples() {
		// FIXME: we really should not do copying of these arrays, as they have about 200,000 elements.
		// Worse yet, commons math makes yet another copy.
		// To fix this, we need to implement our own Quantile.
		List<Double> list = SummaryUtil.getAllChainsLastHalfSamples(d_results, d_parameter);
		double[] arr = new double[list.size()];
		for (int i = 0; i < list.size(); ++i) {
			arr[i] = list.get(i);
		}
		Arrays.sort(arr); // sort so the copy/sort done by Percentile has less impact
		return arr;
	}

	private synchronized void calculateResults() {
		if (!isReady()) return;
		double[] samples = getSamples();
		d_quantiles = new double[d_probabilities.length];
		for(int i = 0; i < d_quantiles.length; i++) {
			d_quantiles[i] = calculateQuantile(i, samples);
		}
		 
		d_defined = true;
		firePropertyChange(PROPERTY_DEFINED, null, d_defined);
	}

	private boolean isReady() {
		if(d_results.getNumberOfSamples() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean getDefined() {
		return d_defined;
	}

	public boolean isContinuous() {
		return d_continuous;
	}

	public void setContinuous(boolean continuous) {
		d_continuous = continuous;
	}
}
