package org.drugis.mtc.summary;

import org.drugis.common.beans.AbstractObservable;
import org.drugis.mtc.Treatment;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;

import java.util.Collections;
import java.util.List;

public class RankProbabilitySummary extends AbstractObservable implements MCMCResultsListener {
	public static final String PROPERTY_VALUE = "value";
	private List<Treatment> d_treatments;

	public RankProbabilitySummary(MCMCResults results, List<Treatment> treatments) {
		d_treatments = treatments;
	}

	public void resultsEvent(MCMCResultsEvent event) {

	}
	
	public List<Treatment> getTreatments() {
		return Collections.unmodifiableList(d_treatments);
	}

	public double getValue(Treatment t, int rank) {
		return 0.0;
	}
}
