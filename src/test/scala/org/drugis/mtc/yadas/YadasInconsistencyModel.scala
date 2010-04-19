package org.drugis.mtc.yadas

class YadasInconsistencyModelIT extends InconsistencyModelTestBase {
	override def makeModel(nm: NetworkModel[DichotomousMeasurement])
	: InconsistencyModel = {
		new YadasInconsistencyModel(nm)
	}
}
