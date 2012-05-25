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