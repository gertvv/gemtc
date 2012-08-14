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

import org.drugis.common.beans.AbstractObservable;
import org.drugis.mtc.MCMCSettings;

public final class YadasSettings extends AbstractObservable implements MCMCSettings {
	private int d_simulationIterations;
	private int d_tuningIterations;
	private final int d_numberOfChains;

	public YadasSettings(int tuningIterations, int simulationIterations, int numberOfChains) {
		d_simulationIterations = simulationIterations;
		d_tuningIterations = tuningIterations;
		d_numberOfChains = numberOfChains;
	}
	
	@Override
	public double getVarianceScalingFactor() {
		return AbstractYadasModel.VARIANCE_SCALING;
	}

	@Override
	public int getTuningIterations() {
		return d_tuningIterations;
	}

	@Override
	public int getThinningInterval() {
		return AbstractYadasModel.THINNING_INTERVAL;
	}

	@Override
	public int getSimulationIterations() {
		return d_simulationIterations;
	}

	@Override
	public int getInferenceSamples() {
		return d_simulationIterations / (2 * AbstractYadasModel.THINNING_INTERVAL) * d_numberOfChains;
	}

	@Override
	public int getNumberOfChains() {
		return d_numberOfChains;
	}
	
	public void setTuningIterations(int newValue) {
		final int oldValue = d_tuningIterations;
		d_tuningIterations = newValue;
		firePropertyChange(PROPERTY_TUNING_ITERATIONS, oldValue, newValue);
	}
	
	public void setSimulationIterations(int newValue) {
		final int oldValue = d_simulationIterations;
		final int oldInferenceSamples = getInferenceSamples();
		d_simulationIterations = newValue;
		firePropertyChange(PROPERTY_SIMULATION_ITERATIONS, oldValue, newValue);
		firePropertyChange(PROPERTY_INFERENCE_SAMPLES, oldInferenceSamples, getInferenceSamples());
	}
}