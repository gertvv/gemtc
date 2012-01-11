package org.drugis.mtc;

import org.apache.commons.collections15.Transformer;

public class ContinuousNetworkBuilder<TreatmentType> extends NetworkBuilder<ContinuousMeasurement, TreatmentType> {
	public ContinuousNetworkBuilder() {
		super();
	}
	
	public ContinuousNetworkBuilder(Transformer<TreatmentType, String> idToString) {
		super(idToString);
	}
	
	public void add(String studyId, TreatmentType treatmentId, double mean, double stdDev, int sampleSize) {
		Treatment t = makeTreatment(treatmentId);
		add(studyId, t, new ContinuousMeasurement(t, mean, stdDev, sampleSize));
	}
}
