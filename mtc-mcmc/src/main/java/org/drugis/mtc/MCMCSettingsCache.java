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
