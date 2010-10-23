package org.drugis.mtc;

public class MCMCResultsEvent {
	private MCMCResults d_source;

	public MCMCResultsEvent(MCMCResults source) {
		d_source = source;
	}

	public MCMCResults getSource() {
		return d_source;
	}
}
