package org.drugis.mtc;

public final class MCMCSettingsCache {

	private final Integer d_inferenceIterations;
	private final Integer d_simulationIterations;
	private final Integer d_thinningInterval;
	private final Integer d_tuningIterations;
	private final Double d_varianceScalingFactor;
	private final Integer d_numberOfChains ;
	
	public MCMCSettingsCache(Integer inferenceIterations, Integer simulationIterations, 
			Integer thinningInterval, Integer tuningIterations, Double varianceScalingFactor, Integer numberOfChains) {
		d_inferenceIterations = inferenceIterations;
		d_simulationIterations = simulationIterations;
		d_thinningInterval = thinningInterval;
		d_tuningIterations = tuningIterations;
		d_varianceScalingFactor = varianceScalingFactor;
		d_numberOfChains = numberOfChains;
	}

	public Double getVarianceScalingFactor() {
		return d_varianceScalingFactor;
	}

	public Integer getTuningIterations() {
		return d_tuningIterations;
	}

	public Integer getThinningInterval() {
		return d_thinningInterval;
	}

	public Integer getSimulationIterations() {
		return d_simulationIterations;
	}

	public Integer getInferenceIterations() {
		return d_inferenceIterations;
	}

	public Integer getNumberOfChains() {
		return d_numberOfChains;
	}	
}
