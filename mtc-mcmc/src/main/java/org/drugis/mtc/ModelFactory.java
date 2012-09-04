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

package org.drugis.mtc;

import java.util.List;

import org.drugis.mtc.model.Network;
import org.drugis.mtc.parameterization.BasicParameter;

/**
 * Factory that creates MTC models from evidence networks.
 */
public interface ModelFactory {
	/**
	 * Create a homogenous variance random effects consistency model.
	 */
	public ConsistencyModel getConsistencyModel(Network network);
	/**
	 * Create a homogenous variance random effects inconsistency model.
	 */
	public InconsistencyModel getInconsistencyModel(Network network);
	/**
	 * Create a homogenous variance random effects node-splitting model.
	 * @param split The node to split on.
	 */
	public NodeSplitModel getNodeSplitModel(Network network, BasicParameter split);
	/**
	 * Generate a list of nodes that are interesting to split.
	 */
	public List<BasicParameter> getSplittableNodes(Network network);
	/**
	 * @return The default settings for the MCMC simulations.
	 */
	public MCMCSettings getDefaults();
	/**
	 * Set the default settings for the MCMC simulations.
	 */
	public void setDefaults(MCMCSettings settings);
}
