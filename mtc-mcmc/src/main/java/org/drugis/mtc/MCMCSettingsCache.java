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

import org.drugis.common.beans.AbstractObservable;

public final class MCMCSettingsCache extends AbstractObservable implements MCMCSettings {

	private final int d_inferenceIterations;
	private final int d_simulationIterations;
	private final int d_thinningInterval;
	private final int d_tuningIterations;
	private final double d_varianceScalingFactor;
	private final int d_numberOfChains ;
	
	public MCMCSettingsCache(int inferenceIterations, int simulationIterations, 
			int thinningInterval, int tuningIterations, double varianceScalingFactor, int numberOfChains) {
		d_inferenceIterations = inferenceIterations;
		d_simulationIterations = simulationIterations;
		d_thinningInterval = thinningInterval;
		d_tuningIterations = tuningIterations;
		d_varianceScalingFactor = varianceScalingFactor;
		d_numberOfChains = numberOfChains;
	}

	@Override
	public double getVarianceScalingFactor() {
		return d_varianceScalingFactor;
	}

	@Override
	public int getTuningIterations() {
		return d_tuningIterations;
	}

	@Override
	public int getThinningInterval() {
		return d_thinningInterval;
	}

	@Override
	public int getSimulationIterations() {
		return d_simulationIterations;
	}

	@Override
	public int getInferenceSamples() {
		return d_inferenceIterations;
	}

	@Override
	public int getNumberOfChains() {
		return d_numberOfChains;
	}
}
