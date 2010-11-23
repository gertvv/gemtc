package org.drugis.mtc.summary;

import org.drugis.common.beans.AbstractObservable;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.convergence.GelmanRubinConvergence;

public class ConvergenceSummary extends AbstractObservable implements Summary {
	public static final String PROPERTY_PSRF = "scaleReduction";
	private final MCMCResults d_results;
	private final Parameter d_parameter;
	private double d_convergence;
	private boolean d_defined = false;

	public ConvergenceSummary(MCMCResults results, Parameter parameter) {
		d_results = results;
		d_parameter = parameter;
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
		d_convergence = GelmanRubinConvergence.diagnose(d_results, d_parameter);
		d_defined = true;
		firePropertyChange(PROPERTY_PSRF, null, d_convergence);
		firePropertyChange(PROPERTY_DEFINED, false, true);
	}
	
	public boolean getDefined() {
		return d_defined;
	}

	public double getScaleReduction() {
		return d_convergence;
	}
}
