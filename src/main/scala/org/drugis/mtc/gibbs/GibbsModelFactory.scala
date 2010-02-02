package org.drugis.mtc.gibbs

import org.drugis.mtc._

/**
 * Construct MTC implementations based on the org.drugis.gibss MCMC package.
 * @see org.drugis.gibbs
 */
class GibbsModelFactory extends ModelFactory {
	def getConsistencyModel(network: Network): MixedTreatmentComparison = null
	def getInconsistencyModel(network: Network): InconsistencyModel = null
}
