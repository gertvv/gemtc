package org.drugis.mtc.parameterization;

import org.drugis.mtc.Parameter;

public class RandomEffectsVariance implements Parameter {
	@Override
	public String getName() {
		return "var.d";
	}

	@Override
	public String toString() {
		return getName();
	}
}
