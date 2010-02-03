package org.drugis.mtc;

public interface ProgressListener {
	public void update(MixedTreatmentComparison mtc, ProgressEvent event);
}
