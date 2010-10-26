/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2010 Gert van Valkenhoef.
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

package org.drugis.mtc.yadas

import org.drugis.mtc._
import gov.lanl.yadas._

import org.apache.commons.math.stat.descriptive.moment.StandardDeviation
import org.apache.commons.math.stat.descriptive.moment.Mean
import org.apache.commons.math.linear.ArrayRealVector

class YadasInconsistencyModel[M <: Measurement](network: Network[M])
extends YadasModel[M, InconsistencyParametrization[M]](network, true)
with InconsistencyModel {
	def this(model: NetworkModel[M, InconsistencyParametrization[M]]) {
		this(model.network)
		proto = model
	}

	override protected def buildNetworkModel() {
		if (proto == null) {
			proto = InconsistencyNetworkModel(network)
		}
	}

	def getInconsistencyVariance: Parameter = inconsistencyVar

	// FIXME: solve below thing with JavaConversions
	def getInconsistencyFactors: java.util.List[Parameter] = {
		val list = new java.util.ArrayList[Parameter]()
		for (param <- proto.inconsistencyParameters) {
			list.add(param)
		}
		list
	}
}
