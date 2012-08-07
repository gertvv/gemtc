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

import org.apache.commons.collections15.BidiMap;
import org.drugis.mtc.NodeSplitModel;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.summary.NodeSplitPValueSummary;

public class SimulationNodeSplitWrapper<TreatmentType> extends AbstractMTCSimulationWrapper<TreatmentType, NodeSplitModel> implements NodeSplitWrapper<TreatmentType> {
	private NodeSplitPValueSummary d_pValueSummary;

	public SimulationNodeSplitWrapper(NodeSplitModel model, BidiMap<TreatmentType, Treatment> treatmentMap) {
		super(model, "Node Split on " + model.getSplitNode().getName(), treatmentMap);
	}

	@Override
	public Parameter getDirectEffect() {
		return d_nested.getDirectEffect();
	}

	@Override
	public Parameter getIndirectEffect() {
		return d_nested.getIndirectEffect();
	}

	@Override
	public BasicParameter getSplitNode() {
		return d_nested.getSplitNode();
	}

	@Override
	public NodeSplitPValueSummary getNodeSplitPValueSummary() {
		if(d_pValueSummary == null) {
			d_pValueSummary = new NodeSplitPValueSummary(d_nested.getResults(), getDirectEffect(), getIndirectEffect());
		}
		return d_pValueSummary;
	}
}
