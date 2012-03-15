/*
 * This file is part of the GeMTC software for MTC model generation and
 * analysis. GeMTC is distributed from http://drugis.org/gemtc.
 * Copyright (C) 2009-2012 Gert van Valkenhoef.
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
