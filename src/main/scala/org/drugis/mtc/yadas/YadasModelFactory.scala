package org.drugis.mtc.yadas

import org.drugis.mtc._

/**
 * Construct MTC implementations based on YADAS 
 */
class YadasModelFactory extends ModelFactory {
	def getConsistencyModel(network: Network): ConsistencyModel = {
		new YadasConsistencyModel(NetworkModel(network))
	}

	def getInconsistencyModel(network: Network): InconsistencyModel = {
		new YadasInconsistencyModel(NetworkModel(network))
	}
}
