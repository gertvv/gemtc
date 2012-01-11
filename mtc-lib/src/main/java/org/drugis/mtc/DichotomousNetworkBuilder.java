package org.drugis.mtc;

public class DichotomousNetworkBuilder<TreatmentType> extends NetworkBuilder<DichotomousMeasurement, TreatmentType> {
	public void add(String studyId, TreatmentType treatmentId, int responders, int sampleSize) {
		Treatment t = makeTreatment(treatmentId);
		add(studyId, t, new DichotomousMeasurement(t, responders, sampleSize));
	}
}
