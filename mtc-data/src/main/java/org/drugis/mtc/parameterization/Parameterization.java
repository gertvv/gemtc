package org.drugis.mtc.parameterization;

import java.util.List;
import java.util.Map;

import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;

import edu.uci.ics.jung.graph.util.Pair;

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
	
	/**
	 * How to parameterize a study. Generates a list, each element of which
	 * represents an independent normal likelihood to be defined. Each of these
	 * elements is itself a list, the elements of which give the comparisons
	 * that are to be parameters of that likelihood.
	 */
	public List<List<Pair<Treatment>>> parameterizeStudy(Study s);
	
	/**
	 * What is the definite baseline in the given study.
	 */
	public Treatment getStudyBaseline(Study s);
}
