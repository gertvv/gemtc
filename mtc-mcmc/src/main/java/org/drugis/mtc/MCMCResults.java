/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
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
	 * Register a listener for changes to the results.
	 */
	public void addResultsListener(MCMCResultsListener l);

	/**
	 * Deregister a listener for changes to the results.
	 */
	public void removeResultsListener(MCMCResultsListener l);

	/**
	 * Clear the results.
	 */
	public void clear();
}
