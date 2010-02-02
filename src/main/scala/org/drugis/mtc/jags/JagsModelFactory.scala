package org.drugis.mtc.jags

import org.drugis.mtc._

/**
 * Construct MTC implementations based directly on JAGS (via JNI).
 * @see fr.iarc.jags
 */
class JagsModelFactory extends ModelFactory {
	def getConsistencyModel(network: Network): MixedTreatmentComparison = null
	def getInconsistencyModel(network: Network): InconsistencyModel = null
}
