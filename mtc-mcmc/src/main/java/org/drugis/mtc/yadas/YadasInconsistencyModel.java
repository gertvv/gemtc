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

import java.util.ArrayList;
import java.util.List;

import org.drugis.mtc.InconsistencyModel;
import org.drugis.mtc.MCMCSettings;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.parameterization.InconsistencyParameterization;
import org.drugis.mtc.parameterization.Parameterization;

public class YadasInconsistencyModel extends YadasModel implements InconsistencyModel {

	public YadasInconsistencyModel(Network network, MCMCSettings settings) {
		super(network, settings);
	}

	public YadasInconsistencyModel(Network network, InconsistencyParameterization pmtz, MCMCSettings settings) {
		super(network, pmtz, settings);
	}
	
	@Override
	protected Parameterization buildNetworkModel() {
		return InconsistencyParameterization.create(d_network);
	}

	@Override
	protected boolean isInconsistency() {
		return true;
	}

	@Override
	public List<Parameter> getInconsistencyFactors() {
		final List<? extends Parameter> parameters = d_pmtz.getParameters();
		return new ArrayList<Parameter>(parameters.subList(getNumberOfBasicParameters(), parameters.size()));
	}

	@Override
	public Parameter getInconsistencyStandardDeviation() {
		return d_inconsistencyStdDev;
	}

}
