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

package org.drugis.mtc;

/**
 * Factory that creates MTC models from evidence networks.
 */
public interface ModelFactory {
	/**
	 * Create a homogenous variance random effects consistency model.
	 */
	public <T extends Measurement> ConsistencyModel getConsistencyModel(Network<T> network);
	/**
	 * Create a homogenous variance random effects inconsistency model.
	 */
	public <T extends Measurement> InconsistencyModel getInconsistencyModel(Network<T> network);
}
