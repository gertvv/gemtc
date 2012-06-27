/*
 * This file is part of ADDIS (Aggregate Data Drug Information System).
 * ADDIS is distributed from http://drugis.org/.
 * Copyright (C) 2009 Gert van Valkenhoef, Tommi Tervonen.
 * Copyright (C) 2010 Gert van Valkenhoef, Tommi Tervonen, 
 * Tijs Zwinkels, Maarten Jacobs, Hanno Koeslag, Florin Schimbinschi, 
 * Ahmad Kamal, Daniel Reid.
 * Copyright (C) 2011 Gert van Valkenhoef, Ahmad Kamal, 
 * Daniel Reid, Florin Schimbinschi.
 * Copyright (C) 2012 Gert van Valkenhoef, Daniel Reid, 
 * Joël Kuiper, Wouter Reckman.
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

package org.drugis.mtc.presentation;

import java.util.List;

import org.drugis.mtc.summary.MultivariateNormalSummary;
import org.drugis.mtc.summary.RankProbabilitySummary;

import edu.uci.ics.jung.graph.util.Pair;

public interface ConsistencyWrapper<TreatmentType> extends MTCModelWrapper<TreatmentType> {

	/**
	 * Return a multivariate summary of the effects for all treatments relative to the baseline. 
	 * The order in which the relative effects are given is based on the natural ordering of the
	 * treatments. The first treatment is used as the baseline.  
	 * 
	 * @see getRelativeEffectsList()
	 * @return A multivariate summary of all the relative effects. 
	 */
	public MultivariateNormalSummary getRelativeEffectsSummary();
	
	public RankProbabilitySummary getRankProbabilities();
	
	/**
	 * @return A list of all <baseline, subject> pairs, where the subjects are given in their natural order  
	 */	
	public List<Pair<TreatmentType>> getRelativeEffectsList();

}
