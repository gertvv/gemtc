package org.drugis.mtc.parameterization;

import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;

/**
 * Starting value generator. If the generator is non-deterministic, repeated invocation of each generator function should generate new independent values.
 */
public interface StartingValueGenerator {
	/**
	 * Generates a starting value for the effect \mu_{i,x}.
	 */
	double getTreatmentEffect(Study study, Treatment treatment);
	/**
	 * Generates a starting value for $\delta_{i,x,y}$
	 */
	double getRelativeEffect(Study study, BasicParameter p);
	/**
	 * Generates a starting value for $d_{x,y}$.
	 */
	double getRelativeEffect(BasicParameter p);
	/**
	 * Generates a starting value for $\sigma$.
	 */
	double getStandardDeviation();
}

