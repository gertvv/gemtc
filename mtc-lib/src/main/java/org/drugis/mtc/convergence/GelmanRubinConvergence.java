package org.drugis.mtc.convergence;

import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.Parameter;

public class GelmanRubinConvergence {
	public static double diagnose(MCMCResults results, Parameter parameter) {
		return diagnose(results, parameter, results.getNumberOfSamples());
	}
	
	/**
	 * Assess convergence based on the first nSamples samples.
	 */
	public static double diagnose(MCMCResults results, Parameter parameter, int nSamples) {
		return 2.0;
	}
}
