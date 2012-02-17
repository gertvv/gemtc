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

import java.util.List;
import java.util.AbstractList;
import java.util.ArrayList;

import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.Parameter;

import org.apache.commons.math.stat.descriptive.StorelessUnivariateStatistic;

public class SummaryUtil {
	/**
	 * A List view on one (parameter, chain) of the MCMCResults.
	 */
	public static class ResultsView extends AbstractList<Double> {
		private final MCMCResults d_results;
		private final int d_param;
		private final int d_chain;
		
		public ResultsView(MCMCResults r, int p, int c) {
			d_results = r;
			d_param = p;
			d_chain = c;
		}
		
		@Override
		public int size() {
			return d_results.getNumberOfSamples();
		}

		@Override
		public Double get(int idx) {
			return d_results.getSample(d_param, d_chain, idx);
		}
	}

	/**
	 * A view on the last half of the supplied list.
	 */
	public static class LastHalfView extends AbstractList<Double> {
		private final List<Double> d_list;

		public LastHalfView(List<Double> list) {
			d_list = list;
		}
		
		@Override
		public int size() {
			return d_list.size() / 2;
		}

		@Override
		public Double get(int idx) {
			return d_list.get(size() + idx);
		}
	}

	/**
	 * A view that concatenates the supplied lists.
	 * All lists are assumed to be of equal length, and it is assumed that at least one list is supplied.
	 */
	public static class ConcatenationView extends AbstractList<Double> {
		private final List<List<Double>> d_lists;

		public ConcatenationView(List<List<Double>> lists) {
			d_lists = lists;
		}

		@Override
		public int size() {
			return d_lists.size() * d_lists.get(0).size();
		}

		@Override
		public Double get(int idx) {
			int size = d_lists.get(0).size();
			return d_lists.get(idx / size).get(idx % size);
		}
	}


	public static double evaluate(StorelessUnivariateStatistic stat, List<Double> vals) {
		stat.clear();
		for (Double d : vals) {
			stat.increment(d);
		}
		return stat.getResult();
	}

	/**
	 * Get the last 1/2 of the samples from each chain in results, for parameter.
	 */
	public static List<Double> getAllChainsLastHalfSamples(MCMCResults results, Parameter parameter) {
		List<List<Double>> lists = new ArrayList<List<Double>>();
		for (int i = 0; i < results.getNumberOfChains(); ++i) {
			lists.add(getOneChainLastHalfSamples(results, parameter, i));
		}
		return new ConcatenationView(lists);
	}
	
	/**
	 * Get the last 1/2 of the samples from one chain in results, for parameter.
	 */
	public static List<Double> getOneChainLastHalfSamples(MCMCResults results, Parameter parameter, int c) {
		int p = results.findParameter(parameter);
		return new LastHalfView(new ResultsView(results, p, c));
	}
}
