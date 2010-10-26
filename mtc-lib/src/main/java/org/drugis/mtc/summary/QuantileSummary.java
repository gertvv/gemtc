package org.drugis.mtc.summary;

import org.drugis.common.beans.AbstractObservable;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;
import org.drugis.mtc.Parameter;

/**
 * Summary of data based on sample quantiles. A list (p_1, ..., p_n) of 
 * probabilities in [0, 1] is given, and the summary will estimate quantiles
 * that correspond to these probabilities. 
 */
public class QuantileSummary extends AbstractObservable implements MCMCResultsListener {
	private static final double[] DEFAULT_PROBABILITIES = new double[] {0.025, 0.5, 0.975};

	public QuantileSummary(MCMCResults results, Parameter parameter, double[] probabilities) {
		
	}
	
	/**
	 * Default probabilities (0.025, 0.5, 0.975): median and 95% interval.
	 */
	public QuantileSummary(MCMCResults results, Parameter parameter) {
		this(results, parameter, DEFAULT_PROBABILITIES);
	}

	public void resultsEvent(MCMCResultsEvent event) {
		// TODO Auto-generated method stub
		
	}
}
