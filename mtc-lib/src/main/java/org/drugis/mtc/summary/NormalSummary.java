package org.drugis.mtc.summary;

import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;
import org.drugis.mtc.Parameter;

public class NormalSummary implements MCMCResultsListener {
	public NormalSummary(MCMCResults results, Parameter parameter) {
		
	}

	public void resultsEvent(MCMCResultsEvent event) {
		// TODO Auto-generated method stub

	}
	
	public boolean isDefined() {
		return false;
	}
	
	public double getMean() {
		return 0.0;
	}
	
	public double getStandardDeviation() {
		return 0.0;
	}

}
