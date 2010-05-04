package org.drugis.mtc.yadas

import org.drugis.mtc._
import gov.lanl.yadas._

class YadasConsistencyModel[M <: Measurement](network: Network[M])
extends YadasModel(network, false) with ConsistencyModel {
	def this(model: NetworkModel[M]) {
		this(model.network)
		proto = model
	}

	var rankCount: Array[Array[Int]] = null

	def rankProbability(t: Treatment, r: Int) = {
		val rIdx = proto.treatmentList.size - r
		val tIdx = proto.treatmentList.indexOf(t)
		rankCount(tIdx)(rIdx).toDouble / simulationIter.toDouble
	}

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
}
