package org.drugis.mtc;

import org.apache.commons.collections15.Transformer;

public class DichotomousNetworkBuilder<TreatmentType> extends NetworkBuilder<DichotomousMeasurement, TreatmentType> {
	public DichotomousNetworkBuilder() {
		super();
	}
	
	public DichotomousNetworkBuilder(Transformer<TreatmentType, String> idToString) {
		super(idToString);
	}
	
	public void add(String studyId, TreatmentType treatmentId, int responders, int sampleSize) {
		Treatment t = makeTreatment(treatmentId);
		add(studyId, t, new DichotomousMeasurement(t, responders, sampleSize));
	}
}
