package org.drugis.mtc.yadas

import org.drugis.mtc._
import org.drugis.mtc.{DichotomousMeasurement => M}

/**
 * Construct MTC implementations based on YADAS 
 */
class YadasModelFactory extends ModelFactory {
	def getConsistencyModel(network: Network[M]): ConsistencyModel = {
		new YadasConsistencyModel(NetworkModel(network))
	}

	def getInconsistencyModel(network: Network[M]): InconsistencyModel = {
		new YadasInconsistencyModel(NetworkModel(network))
	}
}
