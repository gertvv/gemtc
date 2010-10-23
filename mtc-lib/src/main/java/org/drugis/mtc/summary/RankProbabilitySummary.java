package org.drugis.mtc.summary;

import org.drugis.mtc.Treatment;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;
import java.util.List;

// FIXME: this depends on Treatment being available
public class RankProbabilitySummary implements MCMCResultsListener {
	public RankProbabilitySummary(MCMCResults results, List<Treatment> treatments) {
	}

	public void resultsEvent(MCMCResultsEvent event) {

	}

	public double getValue(Treatment t, int rank) {
		return 0.0;
	}
}
