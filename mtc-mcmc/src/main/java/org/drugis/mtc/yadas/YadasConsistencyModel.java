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

import org.drugis.mtc.ConsistencyModel;
import org.drugis.mtc.MCMCSettings;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.parameterization.ConsistencyParameterization;
import org.drugis.mtc.parameterization.Parameterization;

public class YadasConsistencyModel extends YadasModel implements ConsistencyModel {

	public YadasConsistencyModel(Network network, MCMCSettings settings) {
		super(network, settings);
	}

	@Override
	protected Parameterization buildNetworkModel() {
		return ConsistencyParameterization.create(d_network);
	}

	@Override
	protected boolean isInconsistency() {
		return false;
	}
}
