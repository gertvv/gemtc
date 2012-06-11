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

package org.drugis.mtc;

import com.jgoodies.binding.beans.Observable;

public interface MCMCSettings extends Observable {
	
	public static final String PROPERTY_VARIANCE_SCALING_FACTOR = "varianceScalingFactor";
	public static final String PROPERTY_TUNING_ITERATIONS = "tuningIterations";
	public static final String PROPERTY_THINNING_INTERVAL = "thinningInterval";
	public static final String PROPERTY_SIMULATION_ITERATIONS = "simulationIterations";
	public static final String PROPERTY_INFERENCE_SAMPLES = "inferenceSamples";
	public static final String PROPERTY_NUMBER_OF_CHAINS = "numberOfChains";

	public double getVarianceScalingFactor();

	public int getTuningIterations();

	public int getThinningInterval();

	public int getSimulationIterations();

	public int getInferenceSamples();

	public int getNumberOfChains();

}