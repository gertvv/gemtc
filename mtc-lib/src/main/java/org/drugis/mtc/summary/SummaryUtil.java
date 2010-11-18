package org.drugis.mtc.summary;

import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.Parameter;

public class SummaryUtil {

	/**
	 * Get the last 1/2 of the samples from each chain in results, for parameter.
	 */
	public static double[] getAllChainsLastHalfSamples(MCMCResults results, Parameter parameter) {
		int n = results.getNumberOfSamples() / 2;
		int c = results.getNumberOfChains();
		double[] samples = new double[n * c];
		int p = results.findParameter(parameter);
		for (int i = 0; i < c; ++i) {
			System.arraycopy(results.getSamples(p, i), n, samples, i * n, n);
		}
		return samples;
	}
	
	/**
	 * Get the last 1/2 of the samples from one chain in results, for parameter.
	 */
	public static double[] getOneChainLastHalfSamples(MCMCResults results, Parameter parameter, int c) {
		int n = results.getNumberOfSamples() / 2;
		double[] samples = new double[n];
		int p = results.findParameter(parameter);
		System.arraycopy(results.getSamples(p, c), n, samples, 0, n);
		return samples;
	}
}
