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

package org.drugis.mtc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.summary.SummaryUtil;
import org.jfree.data.xy.AbstractXYDataset;

public class EmpiricalDensityDataset extends AbstractXYDataset {

	private static final long serialVersionUID = -9156379642630541775L;
	private static Percentile s_p = new Percentile();
	private int[][] d_counts;
	private double[][] d_densities;
	private double d_interval;
	private double d_bottom;
	private Parameter[] d_parameters;
	private final MCMCResults[] d_results;
	private final int d_nBins;
	private int d_nSeries;
	private double d_top;
	private MCMCResultsListener d_listener = new MCMCResultsListener() {
		public void resultsEvent(MCMCResultsEvent event) {
			calculate();
			fireDatasetChanged();
		}
	};
	
	public static class PlotParameter {
		public final MCMCResults results;
		public final Parameter parameter;
		public PlotParameter(MCMCResults r, Parameter p) {
			results = r;
			parameter = p;
		}
	}
	
	public EmpiricalDensityDataset(int nBins, PlotParameter ... parameters) {
		d_nBins = nBins;
		d_nSeries = parameters.length;
		d_parameters = new Parameter[parameters.length];
		d_results = new MCMCResults[parameters.length];
		d_densities = new double[d_nSeries][d_nBins];
		
		Set<MCMCResults> unique = new HashSet<MCMCResults>();
		for (int i = 0; i < parameters.length; ++i) {
			d_parameters[i] = parameters[i].parameter;
			d_results[i] = parameters[i].results;
			unique.add(d_results[i]);
		}
		for (MCMCResults r : unique) {
			r.addResultsListener(d_listener);
		}
		calculate();
	}

	public EmpiricalDensityDataset(int nBins, MCMCResults r, Parameter p) {
		this(nBins, new PlotParameter(r, p));
	}

	private void calculate() {
		List<Integer> paramIndex = new ArrayList<Integer>();
		
		for(int i = 0; i < d_nSeries; ++i) {
			if (d_results[i].getNumberOfSamples() > 0) {
				paramIndex.add(i);
			}
		}
		
		if (paramIndex.isEmpty()) return; // no samples available
		
		calcBounds(paramIndex);
		calcDensities(paramIndex);
	}

	private void calcBounds(List<Integer> paramIndex) {
		double[] samples = getSamplesArray(paramIndex.get(0));
		d_bottom = s_p.evaluate(samples, 2.5);
		d_top = s_p.evaluate(samples, 97.5);
		for (int j : paramIndex.subList(1, paramIndex.size())) {
			samples = getSamplesArray(j);
			d_bottom = Math.min(s_p.evaluate(samples, 2.5), d_bottom);
			d_top = Math.max(s_p.evaluate(samples, 97.5), d_top);
		}

		d_interval = (d_top - d_bottom) / d_nBins;
	}

	private void calcDensities(List<Integer> paramIndex) {
		d_counts = new int[d_nSeries][d_nBins];
		for (int j : paramIndex) {
			List<Double> samples = getSamples(j);
			double factor = samples.size() * d_interval;
			for (int i = 0; i < samples.size(); ++i) {
				double sample = samples.get(i);
				if (sample >= d_bottom && sample < d_top) {
					int idx = (int) ((sample - d_bottom) / d_interval);
					++d_counts[j][idx];
				}
			}
			for (int i = 0; i < d_nBins; ++i) {
				d_densities[j][i] = d_counts[j][i] / factor; 
			}
		}
	}

	@Deprecated
	private double[] getSamplesArray(int j) { // FIXME: eliminate
		List<Double> list = getSamples(j);
		double[] arr = new double[list.size()];
		for (int i = 0; i < list.size(); ++i) {
			arr[i] = list.get(i);
		}
		Arrays.sort(arr);
		return arr;
	}

	private List<Double> getSamples(int j) {
		return SummaryUtil.getAllChainsLastHalfSamples(d_results[j], d_parameters[j]);
	}

	@Override
	public int getSeriesCount() {
		return d_nSeries;
	}

	@Override
	public Comparable<String> getSeriesKey(int series) {
		return d_parameters[series].getName();
	}

	public Double getX(int series, int bin) {
		if (series < 0 || series >= d_nSeries) throw new IndexOutOfBoundsException();
		return (0.5 + bin) * d_interval + d_bottom; 
	}

	public Double getY(int series, int bin) {
		return d_densities[series][bin];
	}

	public int[] getCounts(int series) {
		return d_counts[series];
	}

	public double[] getDensities(int series) {
		return d_densities[series];
	}

	public int getItemCount(int series) {
		return d_nBins;
	}
	
	double getLowerBound() {
		return d_bottom;
	}
	
	double getUpperBound() {
		return d_top;
	}
}
