package org.drugis.mtc.convergence;

import org.apache.commons.math.stat.correlation.Covariance;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.summary.SummaryUtil;

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
	
	public static double diagnose(MCMCResults results, Parameter parameter) {
		return (new GelmanRubinConvergence(results, parameter)).getCorrPSRF();
		//return diagnose(results, parameter, results.getNumberOfSamples());
	}
	
	/**
	 * Assess convergence, based on the (1st or 2nd?) half of nSamples.
	 */
	public static double diagnose(MCMCResults results, Parameter parameter, int nSamples) {
		return 2.0;
	}
	
	public double oneChainMean(int c){
		return s_mean.evaluate(SummaryUtil.getOneChainLastHalfSamples(d_results, d_parameter, c));
	}

	public double oneChainVar(int c) {
		return s_var.evaluate(SummaryUtil.getOneChainLastHalfSamples(d_results, d_parameter, c));
	}
	
	public double allChainMean() {
		return s_mean.evaluate(SummaryUtil.getAllChainsLastHalfSamples(d_results, d_parameter));
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
