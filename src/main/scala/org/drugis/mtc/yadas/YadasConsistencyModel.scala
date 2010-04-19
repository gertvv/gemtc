package org.drugis.mtc.yadas

import org.drugis.mtc._
import gov.lanl.yadas._

class YadasConsistencyModel[M <: Measurement](network: Network[M])
extends YadasModel(network, false) with ConsistencyModel {
	var rankCount: Array[Array[Int]] = null

	def rankProbability(t: Treatment, r: Int) = {
		if (!isReady) throw new IllegalStateException("Model is not ready")
		if (rankCount == null) initRankCount()

		val rIdx = proto.treatmentList.size - r
		val tIdx = proto.treatmentList.indexOf(t)
		rankCount(tIdx)(rIdx).toDouble / simulationIter.toDouble
	}

	def initRankCount() {
		val baseline = proto.treatmentList(0)
		val data = proto.treatmentList.map(t =>
			if (t == baseline)
				Array.make(simulationIter, 0.0)
			else
				results(new BasicParameter(baseline, t)))
		rankCount = RankCounter.rank(data.toArray)
	}
}
