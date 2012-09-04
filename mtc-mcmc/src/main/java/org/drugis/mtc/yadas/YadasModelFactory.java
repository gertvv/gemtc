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

import java.util.List;

import org.drugis.mtc.ConsistencyModel;
import org.drugis.mtc.InconsistencyModel;
import org.drugis.mtc.MCMCSettings;
import org.drugis.mtc.ModelFactory;
import org.drugis.mtc.NodeSplitModel;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.parameterization.NetworkModel;
import org.drugis.mtc.parameterization.NodeSplitParameterization;

import edu.uci.ics.jung.graph.Hypergraph;

public class YadasModelFactory implements ModelFactory {
	public static final int DEFAULT_TUNING_ITERATIONS = 20000;
	public static final int DEFAULT_SIMULATION_ITERATIONS = 50000;
	public static final int DEFAULT_NUMBER_OF_CHAINS = 4;
	public static final double DEFAULT_VARIANCE_SCALING = 2.5;
	public static final int DEFAULT_THINNING_FACTOR = 10;

	private MCMCSettings d_defaults = new YadasSettings(
			DEFAULT_TUNING_ITERATIONS, DEFAULT_SIMULATION_ITERATIONS, DEFAULT_THINNING_FACTOR,
			DEFAULT_NUMBER_OF_CHAINS, DEFAULT_VARIANCE_SCALING);

	@Override
	public ConsistencyModel getConsistencyModel(Network network) {
		return new YadasConsistencyModel(network, d_defaults);
	}

	@Override
	public InconsistencyModel getInconsistencyModel(Network network) {
		return new YadasInconsistencyModel(network, d_defaults);
	}

	@Override
	public NodeSplitModel getNodeSplitModel(Network network, BasicParameter split) {
		return new YadasNodeSplitModel(network, split, d_defaults);
	}

	@Override
	public List<BasicParameter> getSplittableNodes(Network network) {
		final Hypergraph<Treatment, Study> studyGraph = NetworkModel.createStudyGraph(network);
		return NodeSplitParameterization.getSplittableNodes(studyGraph, NetworkModel.createComparisonGraph(studyGraph));
	}

	@Override
	public MCMCSettings getDefaults() {
		return d_defaults;
	}

	@Override
	public void setDefaults(MCMCSettings settings) {
		d_defaults = settings;
	}
}