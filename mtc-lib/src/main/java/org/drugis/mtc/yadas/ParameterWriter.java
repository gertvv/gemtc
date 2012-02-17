package org.drugis.mtc.yadas;

import gov.lanl.yadas.MCMCParameter;

public abstract class ParameterWriter {
	private final MCMCParameter d_p;
	private final int d_i;

	/**
	 * Create a ParameterWriter that stores the i-th component of p.
	 * @param p An MCMCParameter.
	 * @param i The component to write.
	 */
	public ParameterWriter(MCMCParameter p, int i) {
		d_p = p;
		d_i = i;
	}
	
	public void output() {
		write(d_p.getValue(d_i));
	}

	abstract protected void write(double value);
}
