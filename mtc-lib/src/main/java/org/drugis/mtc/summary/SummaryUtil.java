package org.drugis.mtc.summary;

import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.Parameter;

public class SummaryUtil {

	/**
	 * Get the last 1/2 of the samples from each chain in results, for parameter.
	 */
	public static double[] getSamples(MCMCResults results, Parameter parameter) {
		int n = results.getNumberOfSamples() / 2;
		int c = results.getNumberOfChains();
		double[] samples = new double[n * c];
		int p = results.findParameter(parameter);
		for (int i = 0; i < c; ++i) {
			System.arraycopy(results.getSamples(p, i), n, samples, i * n, n);
		}
		return samples;
	}

}
