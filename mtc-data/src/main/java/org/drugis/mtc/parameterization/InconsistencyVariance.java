package org.drugis.mtc.parameterization;

import org.drugis.mtc.Parameter;

public class InconsistencyVariance implements Parameter {
	@Override
	public String getName() {
		return "var.w";
	}

	@Override
	public String toString() {
		return getName();
	}
}
