package org.drugis.mtc;

import org.drugis.mtc.jags.JagsSyntaxModel;
import org.drugis.mtc.parameterization.StartingValueGenerator;

public class ModelSpecification {
	private final JagsSyntaxModel d_model;
	private final StartingValueGenerator d_generator;
	private final String d_nameSuffix;

	public ModelSpecification(JagsSyntaxModel model, StartingValueGenerator generator, String nameSuffix) {
		d_model = model;
		d_generator = generator;
		d_nameSuffix = nameSuffix;
	}

	public JagsSyntaxModel getModel() {
		return d_model;
	}

	public StartingValueGenerator getGenerator() {
		return d_generator;
	}

	public String getNameSuffix() {
		return d_nameSuffix;
	}
}
