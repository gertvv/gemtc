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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.drugis.mtc.MCMCSettingsCache;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.InconsistencyParameter;
import org.drugis.mtc.parameterization.InconsistencyVariance;
import org.drugis.mtc.summary.ConvergenceSummary;
import org.drugis.mtc.summary.QuantileSummary;

public class SavedInconsistencyWrapper<TreatmentType> extends AbstractMTCSavedWrapper<TreatmentType> implements InconsistencyWrapper<TreatmentType> {

	private List<Parameter> d_inconsistencyFactors;

	public SavedInconsistencyWrapper(MCMCSettingsCache settings, Map<Parameter, QuantileSummary> quantileSummaries,
			Map<Parameter, ConvergenceSummary> convergenceSummaries, Map<TreatmentType, Treatment> treatmentMap) {
		super(settings, quantileSummaries, convergenceSummaries, treatmentMap);
		d_inconsistencyFactors = new ArrayList<Parameter>();
		for(Parameter p : d_quantileSummaries.keySet()) { 
			if((p instanceof InconsistencyParameter)) {
				d_inconsistencyFactors.add(p);
			}
		}
	}

	@Override
	public List<Parameter> getInconsistencyFactors() {
		return d_inconsistencyFactors;
	}
	
	@Override
	public Parameter getInconsistencyVariance() {
		for(Parameter p : d_quantileSummaries.keySet()) { 
			if(p instanceof InconsistencyVariance) {
				return p;
			}
		}
		return null;
	}
	
	@Override
	public String getDescription() {
		return "Inconsistency Model";
	}
}
