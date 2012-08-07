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

package org.drugis.mtc.presentation;

import java.util.HashMap;
import java.util.Map;

import org.drugis.common.beans.AbstractObservable;
import org.drugis.mtc.MCMCModel;
import org.drugis.mtc.MCMCSettings;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.summary.ConvergenceSummary;
import org.drugis.mtc.summary.QuantileSummary;

public class MCMCSimulationWrapper<ModelType extends MCMCModel> extends AbstractObservable implements MCMCModelWrapper {
	protected final ModelType d_nested;
	private final Map<Parameter, QuantileSummary> d_quantileSummaryMap = new HashMap<Parameter, QuantileSummary>();
	private final Map<Parameter, ConvergenceSummary> d_convergenceSummaryMap = new HashMap<Parameter, ConvergenceSummary>();
	private boolean d_destroy = false;
	private final String d_description;

	public MCMCSimulationWrapper(ModelType mtc, String description) {
		d_nested = mtc;
		d_description = description;
	}

	@Override
	public ModelType getModel() {
		return d_nested;
	}

	@Override
	public boolean isSaved() { 
		return false;
	}

	@Override
	public boolean isApproved() {
		return d_nested.getActivityTask().isFinished();
	}

	@Override
	public MCMCSettings getSettings() {
		return d_nested.getSettings();
	}

	@Override
	public QuantileSummary getQuantileSummary(Parameter p) {
		if(d_quantileSummaryMap.get(p) == null) { 
			d_quantileSummaryMap.put(p, new QuantileSummary(d_nested.getResults(), p));
		}
		return d_quantileSummaryMap.get(p);
	}

	@Override
	public ConvergenceSummary getConvergenceSummary(Parameter p) {
		if(d_convergenceSummaryMap.get(p) == null) { 
			d_convergenceSummaryMap.put(p, new ConvergenceSummary(d_nested.getResults(), p));
		}
		return d_convergenceSummaryMap.get(p);
	}

	@Override
	public Parameter[] getParameters() { 
		return d_nested.getResults().getParameters();
	}

	@Override
	public void selfDestruct() {
		d_destroy  = true;
		firePropertyChange(PROPERTY_DESTROYED, false, true);
	}

	@Override
	public boolean getDestroyed() { 
		return d_destroy;
	}

	@Override
	public String getDescription() {
		return d_description;
	}
}