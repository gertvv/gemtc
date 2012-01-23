package org.drugis.mtc.parameterization;

import java.util.List;
import java.util.Map;

import org.drugis.mtc.model.Treatment;

/**
 * Parameterization of a Network. The parameterization completely fixes the 
 * structure of the MCMC model, but is not concerned with values (e.g. data, 
 * prior parameters, starting values). 
 */
public interface Parameterization {
	/**
	 * Get the parameters for the relative effect structure.
	 * @return A list of parameters, in a definite order.
	 * @see NetworkParameterComparator
	 */
	public List<NetworkParameter> getParameters();
	
	/**
	 * Express the effect of 'subj' relative to 'base' in terms of the parameters.
	 * @param base Baseline treatment.
	 * @param subj Subject of comparison.
	 * @return The parameterization as a linear combination of parameters.
	 * @see Parameterization.getParameters
	 */
	public Map<NetworkParameter, Integer> parameterize(Treatment base, Treatment subj);

}
