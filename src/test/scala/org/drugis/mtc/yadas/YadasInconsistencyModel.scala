package org.drugis.mtc.yadas

class YadasInconsistencyModelTest extends InconsistencyModelTestBase {
	override def makeModel(nm: NetworkModel): InconsistencyModel = {
		new YadasInconsistencyModel(nm)
	}
}
