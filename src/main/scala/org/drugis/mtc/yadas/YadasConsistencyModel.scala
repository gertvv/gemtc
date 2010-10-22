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

class YadasConsistencyModel[M <: Measurement](network: Network[M])
extends YadasModel[M, ConsistencyParametrization[M]](network, false)
with ConsistencyModel {
	def this(model: NetworkModel[M, ConsistencyParametrization[M]]) {
		this(model.network)
		proto = model
	}

	override protected def buildNetworkModel() {
		if (proto == null) {
			proto = ConsistencyNetworkModel(network)
		}
	}

//	var rankCount: Array[Array[Int]] = null

	def rankProbability(t: Treatment, r: Int) = {
/*
		val rIdx = proto.treatmentList.size - r
		val tIdx = proto.treatmentList.indexOf(t)
		rankCount(tIdx)(rIdx).toDouble / simulationIter.toDouble
*/
		0.0
	}

/*
	override def output() {
		super.output()
		updateRankCounts()
	}

	private def updateRankCounts() {
		if (rankCount == null) {
			rankCount = proto.treatmentList.map(
				t => Array.make(proto.treatmentList.size, 0)).toArray
		}

		val baseline = proto.treatmentList(0)
		val data = proto.treatmentList.map(t =>
			if (t == baseline) 0.0
			else parameters(new BasicParameter(baseline, t)).getValue)
		val ranks = RankCounter.rank(data.toArray)

		for (j <- 0 until ranks.size) {
			rankCount(j)(ranks(j) - 1) += 1
		}
	}
*/
}
