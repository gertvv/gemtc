package org.drugis.mtc.yadas

import org.drugis.mtc._

/**
 * Construct MTC implementations based on YADAS 
 */
class YadasModelFactory extends ModelFactory {
	def getConsistencyModel(network: Network): MixedTreatmentComparison = null
	def getInconsistencyModel(network: Network): InconsistencyModel = {
		new YadasInconsistencyModel(NetworkModel(network))
	}
}
