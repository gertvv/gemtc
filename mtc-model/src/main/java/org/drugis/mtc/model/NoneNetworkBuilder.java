package org.drugis.mtc.model;

import org.apache.commons.collections15.Transformer;
import org.drugis.mtc.data.DataType;

public class NoneNetworkBuilder<TreatmentType> extends NetworkBuilder<TreatmentType> {
	public static NoneNetworkBuilder<Treatment> createSimple() {
		return new NoneNetworkBuilder<Treatment>(
				new NetworkBuilder.TreatmentIdTransformer(),
				new NetworkBuilder.TreatmentDescriptionTransformer());
	}

	public NoneNetworkBuilder() {
		super(DataType.NONE);
	}
	
	public NoneNetworkBuilder(Transformer<TreatmentType, String> treatmentToIdString, Transformer<TreatmentType, String> treatmentToDescription) {
		super(treatmentToIdString, treatmentToDescription, DataType.NONE);
	}
	
	public void add(String studyId, TreatmentType treatmentId) {
		Treatment t = makeTreatment(treatmentId);
		add(studyId, t, new Measurement(t));
	}
}