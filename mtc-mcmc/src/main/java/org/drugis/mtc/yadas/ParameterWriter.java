/*
 * This file is part of the GeMTC software for MTC model generation and
 * analysis. GeMTC is distributed from http://drugis.org/gemtc.
 * Copyright (C) 2009-2012 Gert van Valkenhoef.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
