/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2010 Gert van Valkenhoef.
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

package org.drugis.mtc.util;

/**
 * Represents a point estimate with associated standard error.
 */
public class EstimateWithPrecision {
	private final double d_pe;
	private final double d_se;

	/**
	 * @param pe The point estimate.
	 * @param se The standard error for the point estimate.
	 */
	public EstimateWithPrecision(double pe, double se) {
		d_pe = pe;
		d_se = se;
	}

	public double getPointEstimate() {
		return d_pe;
	}

	public double getStandardError() {
		return d_se;
	}
}
