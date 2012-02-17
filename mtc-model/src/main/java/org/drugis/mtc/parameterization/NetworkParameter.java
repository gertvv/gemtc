package org.drugis.mtc.parameterization;

import org.drugis.mtc.Parameter;

public interface NetworkParameter extends Parameter {
	/**
	 * Get the name of this parameter.
	 * May contain only letters, digits, underscores and periods.
	 */
	public String getName();
}
