package org.drugis.mtc.summary;

import org.drugis.common.beans.AbstractObservable;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;
import org.drugis.mtc.Parameter;

public class NodeSplitPValueSummary extends AbstractObservable implements
		Summary {

	public static final String PROPERTY_PVALUE = "pValue";
	private MCMCResults d_results;
	private Parameter d_direct;
	private Parameter d_indirect;
	private boolean d_defined;
	private double d_pvalue;

	public NodeSplitPValueSummary(MCMCResults r, Parameter dir, Parameter indir) {
		d_results = r;
		d_direct = dir;
		d_indirect = indir;
		d_defined = false;
		
		d_results.addResultsListener(new MCMCResultsListener() {
			public void resultsEvent(MCMCResultsEvent event) {
				calc();
			}
		});		
		if (d_results.getNumberOfSamples() > 0) {
			calc();
		}
	}
	
	private void calc() {
		calculatePValue();
		d_defined = true;
		firePropertyChange(PROPERTY_PVALUE, null, d_pvalue);
		firePropertyChange(PROPERTY_DEFINED, false, true);	
	}

	private void calculatePValue() {
		int nDirLargerThanIndir = 0;
		double [] directSamples = SummaryUtil.getAllChainsLastHalfSamples(d_results, d_direct);
		double [] indirectSamples = SummaryUtil.getAllChainsLastHalfSamples(d_results, d_indirect);
		for (int i = 0; i < directSamples.length; ++i) {
			if (directSamples[i] > indirectSamples[i]) ++nDirLargerThanIndir;
		}
		double prop = (double) nDirLargerThanIndir / (double) directSamples.length;
		d_pvalue = 2.0 * Math.min(prop, 1.0 - prop);
	}

	public boolean getDefined() {
		return d_defined;
	}
	
	public double getPvalue() {
		return d_pvalue;
	}

}
