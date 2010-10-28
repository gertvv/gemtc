package org.drugis.mtc.summary;

import org.drugis.common.beans.AbstractObservable;
import org.drugis.mtc.Treatment;
import org.drugis.mtc.BasicParameter;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;
import org.drugis.mtc.RankCounter;

import java.util.Collections;
import java.util.List;

public class RankProbabilitySummary extends AbstractObservable implements MCMCResultsListener {
	public static final String PROPERTY_VALUE = "value";
	private List<Treatment> d_treatments;
	private MCMCResults d_results;
	private int d_n;
	private int[][] d_rankCount;
	private int d_samples;

	public RankProbabilitySummary(MCMCResults results, List<Treatment> treatments) {
		d_treatments = treatments;
		d_n = treatments.size();
		calculate();
	}

	public void resultsEvent(MCMCResultsEvent event) {
		calculate();
		firePropertyChange(PROPERTY_VALUE, null, this);
	}
	
	public List<Treatment> getTreatments() {
		return Collections.unmodifiableList(d_treatments);
	}

	private boolean isDefined() {
		return d_samples > 0;
	}

	public double getValue(Treatment t, int rank) {
		if (!isDefined()) {
			return 0.0;
		}
		int rIdx = d_n - rank;
		int tIdx = d_treatments.indexOf(t);
		return ((double)d_rankCount[tIdx][rIdx]) / ((double)d_samples);
	}

	// FIXME: handle multiple chains
	private synchronized void calculate() {
		d_samples = d_results.getNumberOfSamples();
		d_rankCount = new int[d_n][d_n];

		int[] idx = new int[d_n];
		Treatment base = d_treatments.get(0);
		for (int i = 1; i < d_n; ++i ) {
			idx[i] = d_results.findParameter(new BasicParameter(base, d_treatments.get(i)));
		}

		for (int i = 0; i < d_samples; ++i) {
			double[] data = new double[d_n];
			for (int j = 1; j < d_n; ++j) {
				data[j] = d_results.getSample(idx[j], 0, i);
			}
			int[] ranks = RankCounter.rank(data);
			for (int j = 0; j < d_n; ++j) {
				d_rankCount[j][ranks[j] - 1] += 1;
			}
		}
	}
}
