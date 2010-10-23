package org.drugis.mtc;

/**
 * Results-container for MCMC simulation. 
 */
public interface MCMCResults {
	/**
	 * This returns only the basic parameters (i.e., those for which actual samples were generated).
	 * Implementations may include additional derived parameters, which will be indexed starting at
	 * getParamters().length.
	 * @see findParameter
	 * @return A list of parameters for which results are available.
	 */
	public Parameter[] getParameters();
	
	/**
	 * Find a parameter's index. This is not equi
	 * @param p The parameter.
	 * @return The parameter's index, to be used when retrieving results.
	 */
	public int findParameter(Parameter p);
	
	/**
	 * @return The number of parallel chains that was/is/will be run.
	 */
	public int getNumberOfChains();
	
	/**
	 * @return The number of samples that are available for all parameter/chain combinations.
	 */
	public int getNumberOfSamples();

	/**
	 * Get an MCMC sample.
	 * @param p The parameter index.
	 * @param c The chain index.
	 * @param i The sample index.
	 * @return A sample.
	 */
	public double getSample(int p, int c, int i);
	
	/**
	 * Get all MCMC samples for a parameter within a chain.
	 * @param p The parameter index.
	 * @param c The chain index.
	 * @return An array of getNumberOfSamples() samples.
	 */
	public double[] getSamples(int p, int c);

	/**
	 * Register a listener for changes to the results.
	 */
	public void addResultsListener(MCMCResultsListener l);

	/**
	 * Deregister a listener for changes to the results.
	 */
	public void removeResultsListener(MCMCResultsListener l);
}
