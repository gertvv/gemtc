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
