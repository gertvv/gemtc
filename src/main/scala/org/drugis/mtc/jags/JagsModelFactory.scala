package org.drugis.mtc.jags

import org.drugis.mtc._

import fr.iarc.jags.ModuleManager

/**
 * Construct MTC implementations based directly on JAGS (via JNI).
 * @see fr.iarc.jags
 */
class JagsModelFactory extends ModelFactory {
	def getConsistencyModel(network: Network): ConsistencyModel = null
	def getInconsistencyModel(network: Network): InconsistencyModel = {
		JagsModelFactory.loadModules()
		new JagsJniInconsistencyModel(NetworkModel(network))
	}
}

object JagsModelFactory {
	private var loaded = false
	def loadModules() {
		if (!loaded) {
			ModuleManager.loadModule("basemod")
			ModuleManager.loadModule("bugs")
			loaded = true
		}
	}
}
