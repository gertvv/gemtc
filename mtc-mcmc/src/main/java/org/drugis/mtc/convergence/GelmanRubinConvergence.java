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

package org.drugis.mtc.convergence;

import org.apache.commons.math.stat.correlation.Covariance;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.summary.SummaryUtil;
import org.drugis.mtc.util.WindowResults;

public class GelmanRubinConvergence {
	
	private MCMCResults d_results;
	private Parameter d_parameter;
	private static Mean s_mean = new Mean();
	private static Variance s_var = new Variance();

	public GelmanRubinConvergence(MCMCResults results, Parameter parameter) {
		assert(results.getNumberOfSamples() % 2 == 0);
		d_results = results;
		d_parameter = parameter;
	}
	
	/**
	 * Assess convergence, based on the 2nd half of the samples.
	 */
	public static double diagnose(MCMCResults results, Parameter parameter) {
		return (new GelmanRubinConvergence(results, parameter)).getCorrPSRF();
	}
	
	/**
	 * Assess convergence diagnostic \hat{R}, restricting results to only the first nSamples.
	 */
	public static double diagnose(MCMCResults results, Parameter parameter, int nSamples) {
		return diagnose(new WindowResults(results, 0, nSamples), parameter);
	}

	/**
	 * Calculate the pooled posterior variance estimate, \hat{V}.
	 */
	public static double calculatePooledVariance(MCMCResults results, Parameter parameter, int nSamples) {
		return (new GelmanRubinConvergence(new WindowResults(results, 0, nSamples), parameter)).getVHat();
	}

	/**
	 * Calculate the within-chain variance estimate, W.
	 */
	public static double calculateWithinChainVariance(MCMCResults results, Parameter parameter, int nSamples) {
		return (new GelmanRubinConvergence(new WindowResults(results, 0, nSamples), parameter)).getWithinChainVar();
	}
	
	public double oneChainMean(int c){
		return SummaryUtil.evaluate(s_mean, SummaryUtil.getOneChainLastHalfSamples(d_results, d_parameter, c));
	}

	public double oneChainVar(int c) {
		return SummaryUtil.evaluate(s_var, SummaryUtil.getOneChainLastHalfSamples(d_results, d_parameter, c));
	}
	
	public double allChainMean() {
		return SummaryUtil.evaluate(s_mean, SummaryUtil.getAllChainsLastHalfSamples(d_results, d_parameter));
	}

	public double getBetweenChainVar() {
		double var = 0;
		double mean = allChainMean();
		for(int i=0; i< d_results.getNumberOfChains(); ++i) {
			var += java.lang.Math.pow(oneChainMean(i) - mean, 2);
		}
		return (d_results.getNumberOfSamples() * var / 2) / (d_results.getNumberOfChains() - 1);
	}
	
	public int getNSamples() {
		return d_results.getNumberOfSamples() / 2;
	}

	public int getNChains() {
		return d_results.getNumberOfChains();
	}

	public double getWithinChainVar() {
		return s_mean.evaluate(getVariances());
	}

	public double[] getVariances() {
		double [] tmp = new double[getNChains()];
		for(int i=0; i<getNChains(); ++i) {
			tmp[i] = oneChainVar(i);
		}
		return tmp;
	}

	public double[] getMeans() {
		double [] tmp = new double[getNChains()];
		for(int i=0; i<getNChains(); ++i) {
			tmp[i] = oneChainMean(i);
		}
		return tmp;
	}
	
	public double getSigmaSquaredHat() {
		int n = getNSamples();
		return getWithinChainVar() * (n - 1) / n + getBetweenChainVar() / n;
	}
	
	public double getVHat() {
		return getSigmaSquaredHat() + getBetweenChainVar() / (d_results.getNumberOfChains() * getNSamples());
	}

	public double getCorrPSRF() {
		double d = getDegreesOfFreedom();
		double dfactor = (d + 3) / (d + 1);
		return Math.sqrt(dfactor * getVHat() / getWithinChainVar());
	}

	public double getDegreesOfFreedom() {
		double m = getNChains();
		double n = getNSamples();
		Covariance cov = new Covariance();
 
		double [] squaredMeans = getMeans();
		for (int i = 0; i < getNChains(); ++i) squaredMeans[i] *= squaredMeans[i]; 
		
		double varW = s_var.evaluate(getVariances()) / m;
		double varB = 2 * getBetweenChainVar() * getBetweenChainVar() / (m - 1);
		
		double covWB = (n / m) * (cov.covariance(getVariances(), squaredMeans) - 2 
						* allChainMean() * cov.covariance(getVariances(), getMeans()));
		double varV = ( Math.pow(n - 1, 2) * varW + Math.pow(1 + 1 / m, 2) 
						* varB + 2 * (n - 1) * (1 + 1 / m) * covWB) / (n * n); 
		return 2 * getVHat() * getVHat() / varV;
	}
	
}
