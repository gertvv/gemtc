package org.drugis.mtc;

/**
 * A MixedTreatmentComparison estimates the relative effects of a set of
 * treatments given an evidence network. The estimates are only provided after
 * the model has been run (using Runnable.run()). It is possible to track the
 * progress of the model run using a ProgressListener.
 */
public interface MixedTreatmentComparison extends Runnable {
	/**
	 * Get the estimated relative effect.
	 * @return The effect estimate.
	 * @param base The treatment to use as baseline.
	 * @param subj The treatment to use as alternative.
	 * @throws IllegalArgumentException if one of the treatments is not
	 * present in the evidence network.
	 * @throws IllegalStateException if the MTC is not ready.
	 */
	public Estimate getRelativeEffect(Treatment base, Treatment subj);
	/**
	 * Add a progress listener to this MTC.
	 */
	public void addProgressListener(ProgressListener l);
	/**
	 * @return false if it's necessary to run() this model before calling any
	 * getters.
	 */
	public boolean isReady();
	/**
	 * @return the number of burn-in iterations
	 */
	public int getBurnInIterations();
	/**
	 * @param it The number of burn-in iterations, a multiple of 100.
	 * @throws IllegalArgumentException if it is not a multiple of 100, or
	 * if it <= 0.
	 */
	public void setBurnInIterations(int it);
	/**
	 * @return the number of simulation iterations
	 */
	public int getSimulationIterations();
	/**
	 * @param it The number of simulation iterations, a multiple of 100.
	 * @throws IllegalArgumentException if it is not a multiple of 100, or
	 * if it <= 0.
	 */
	public void setSimulationIterations(int it);
}
