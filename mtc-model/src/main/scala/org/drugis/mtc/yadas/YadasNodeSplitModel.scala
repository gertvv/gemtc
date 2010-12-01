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

class YadasNodeSplitModel[M <: Measurement](network: Network[M],
	val splitNode: BasicParameter)
extends YadasModel[M, NodeSplitParametrization[M]](network, false)
with NodeSplitModel {
	def this(model: NetworkModel[M, NodeSplitParametrization[M]]) {
		this(model.network, new BasicParameter(
				model.parametrization.splitNode._1,
				model.parametrization.splitNode._2)
		)
		proto = model
	}

	override protected def buildNetworkModel() {
		if (proto == null) {
			proto = NodeSplitNetworkModel(network,
				(splitNode.base, splitNode.subject))
		}
	}

	def getSplitNode = splitNode

	def getDirectEffect =
		new SplitParameter(splitNode.base, splitNode.subject, true)
	def getIndirectEffect =
		new SplitParameter(splitNode.base, splitNode.subject, false)
}
