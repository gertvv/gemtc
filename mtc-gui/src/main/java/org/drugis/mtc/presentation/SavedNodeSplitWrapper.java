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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.drugis.mtc.MCMCSettingsCache;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.parameterization.SplitParameter;
import org.drugis.mtc.summary.ConvergenceSummary;
import org.drugis.mtc.summary.NodeSplitPValueSummary;
import org.drugis.mtc.summary.QuantileSummary;

public class SavedNodeSplitWrapper<TreatmentType> extends AbstractMTCSavedWrapper<TreatmentType> implements NodeSplitWrapper<TreatmentType> {

	private final NodeSplitPValueSummary d_nodeSplitPValueSummary;
	private final BasicParameter d_splitNode;

	public SavedNodeSplitWrapper(MCMCSettingsCache settings,
			Map<Parameter, QuantileSummary> quantileSummaries,
			Map<Parameter, ConvergenceSummary> convergenceSummaries,
			BasicParameter splitNode,
			NodeSplitPValueSummary nodeSplitPValueSummary, 
			Map<TreatmentType, Treatment> treatmentMap) {
		super(settings, quantileSummaries, convergenceSummaries, treatmentMap);
		d_splitNode = splitNode;
		d_nodeSplitPValueSummary = nodeSplitPValueSummary;
	}

	@Override
	public Parameter getDirectEffect() {
		for(Parameter p : d_quantileSummaries.keySet()) { 
			if(p instanceof SplitParameter && ((SplitParameter) p).isDirect()) {
				return p;
			}
		}
		return null;
	}

	@Override
	public Parameter getIndirectEffect() {
		for(Parameter p : d_quantileSummaries.keySet()) { 
			if(p instanceof SplitParameter && !((SplitParameter) p).isDirect()) {
				return p;
			}
		}
		return null;
	}

	@Override
	public Parameter getSplitNode() {
		return d_splitNode;
	}

	@Override
	public NodeSplitPValueSummary getNodeSplitPValueSummary() {
		return d_nodeSplitPValueSummary;
	}

	@Override
	public String getDescription() {
		return "Node Split on " + getSplitNode().getName();
	}
	
	@Override
	public Parameter[] getParameters() { 
		Set<Parameter> set = new HashSet<Parameter>(d_convergenceSummaries.keySet());
		set.remove(getIndirectEffect());
		return set.toArray(new Parameter[] {});
	}

}
