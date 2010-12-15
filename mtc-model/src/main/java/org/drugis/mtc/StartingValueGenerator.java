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
 * Starting value generator. If the generator is non-deterministic, repeated invocation of each generator function should generate new independent values.
 */
abstract public class StartingValueGenerator<M extends Measurement> {
	/**
	 * Generates a starting value for $\mu_i$.
	 */
	abstract public double getBaselineEffect(Study<M> study);
	/**
	 * Generates a starting value for $\delta_{i,x,y}$
	 */
	abstract public double getRandomEffect(Study<M> study, BasicParameter p);
	/**
	 * Generates a starting value for $d_{x,y}$.
	 */
	abstract public double getRelativeEffect(BasicParameter p);
	/**
	 * Generates a starting value for $\sigma$.
	 */
	abstract public double getRandomEffectsVariance();
}
