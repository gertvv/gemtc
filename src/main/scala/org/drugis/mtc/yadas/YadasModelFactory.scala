package org.drugis.mtc.yadas

import org.drugis.mtc._

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
}
