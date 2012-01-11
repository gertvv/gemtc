package org.drugis.mtc;

public class ContinuousNetworkBuilder extends NetworkBuilder<ContinuousMeasurement> {
	public void add(String studyId, String treatmentId, double mean, double stdDev, int sampleSize) {
		Treatment t = makeTreatment(treatmentId);
		add(studyId, t, new ContinuousMeasurement(t, mean, stdDev, sampleSize));
	}
}
