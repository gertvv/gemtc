/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
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

import scala.collection.JavaConversions._

/**
 * Construct MTC implementations based on YADAS 
 */
class YadasModelFactory extends ModelFactory {
	def getConsistencyModel[M <: Measurement](network: Network[M])
	: ConsistencyModel = {
		new YadasConsistencyModel(network)
	}

	def getInconsistencyModel[M <: Measurement](network: Network[M])
	: InconsistencyModel = {
		new YadasInconsistencyModel(network)
	}

	def getNodeSplitModel[M <: Measurement](network: Network[M], 
		split: BasicParameter)
	: NodeSplitModel = {
		new YadasNodeSplitModel(network, split)
	}

	def getSplittableNodes[M <: Measurement](network: Network[M])
	: java.util.List[BasicParameter] = {
		NodeSplitNetworkModel.getSplittableNodes(network).map(e => new BasicParameter(e._1, e._2)).toList
	}
}
