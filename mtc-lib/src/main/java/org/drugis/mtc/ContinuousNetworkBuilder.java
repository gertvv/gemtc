package org.drugis.mtc;

public class ContinuousNetworkBuilder<TreatmentType> extends NetworkBuilder<ContinuousMeasurement, TreatmentType> {
	public void add(String studyId, TreatmentType treatmentId, double mean, double stdDev, int sampleSize) {
		Treatment t = makeTreatment(treatmentId);
		add(studyId, t, new ContinuousMeasurement(t, mean, stdDev, sampleSize));
	}
}
