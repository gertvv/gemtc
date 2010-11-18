package org.drugis.mtc.convergence;

import gov.lanl.yadas.PowerBondArray;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.summary.SummaryUtil;

import scala.Math;

public class GelmanRubinConvergence {
	
	private MCMCResults d_results;
	private Parameter d_parameter;
	private static Mean s_mean = new Mean();
	private static Variance s_var = new Variance();

	public GelmanRubinConvergence(MCMCResults results, Parameter parameter) {
		d_results = results;
		d_parameter = parameter;
	}
	
	public static double diagnose(MCMCResults results, Parameter parameter) {
		return diagnose(results, parameter, results.getNumberOfSamples());
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

	public double varBetweenChains() {
		double var = 0;
		double mean = allChainMean();
		for(int i=0; i< d_results.getNumberOfChains(); ++i) {
			var += java.lang.Math.pow(oneChainMean(i) - mean, 2);
		}
		return (d_results.getNumberOfSamples() * var / 2) / (d_results.getNumberOfChains() - 1);
	}
}
