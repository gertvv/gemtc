package org.drugis.mtc;

public class DichotomousNetworkBuilder extends NetworkBuilder<DichotomousMeasurement> {
	public void add(String studyId, String treatmentId, int responders, int sampleSize) {
		Treatment t = makeTreatment(treatmentId);
		add(studyId, t, new DichotomousMeasurement(t, responders, sampleSize));
	}
}
