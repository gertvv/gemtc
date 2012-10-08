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

package org.drugis.mtc.yadas;

import java.util.Map;

import org.drugis.mtc.MCMCSettings;
import org.drugis.mtc.NodeSplitModel;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.parameterization.NetworkParameter;
import org.drugis.mtc.parameterization.NodeSplitParameterization;
import org.drugis.mtc.parameterization.Parameterization;
import org.drugis.mtc.parameterization.SplitParameter;

public class YadasNodeSplitModel extends YadasModel implements NodeSplitModel {
	private final BasicParameter d_split;

	public YadasNodeSplitModel(Network network, BasicParameter split, MCMCSettings settings) {
		super(network, settings);
		d_split = split;
	}

	public YadasNodeSplitModel(Network network, NodeSplitParameterization pmtz, MCMCSettings settings) {
		super(network, pmtz, settings);
		d_split = pmtz.getSplitNode();
	}

	@Override
	protected Parameterization buildNetworkModel() {
		return NodeSplitParameterization.create(d_network, d_split);
	}

	@Override
	protected Map<NetworkParameter, Derivation> getDerivedParameters() {
		final Map<NetworkParameter, Derivation> map = super.getDerivedParameters();
		map.put(getIndirectEffect(), new Derivation(((NodeSplitParameterization)d_pmtz).parameterizeIndirect()));
		return map;
	}

	@Override
	protected boolean isInconsistency() {
		return false;
	}

	@Override
	public NetworkParameter getDirectEffect() {
		return new SplitParameter(d_split.getBaseline(), d_split.getSubject(), true);
	}

	@Override
	public NetworkParameter getIndirectEffect() {
		return new SplitParameter(d_split.getBaseline(), d_split.getSubject(), false);
	}

	@Override
	public BasicParameter getSplitNode() {
		return d_split;
	}
}
