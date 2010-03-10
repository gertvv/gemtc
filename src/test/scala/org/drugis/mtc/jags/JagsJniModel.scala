package org.drugis.mtc.jags

import org.drugis.mtc.{
	InconsistencyModelTestBase,InconsistencyModel,NetworkModel}

class JagsJniInconsistencyModelTest extends InconsistencyModelTestBase {
	override def makeModel(nm: NetworkModel): InconsistencyModel = {
		JagsModelFactory.loadModules()
		new JagsJniInconsistencyModel(nm)
	}
}
