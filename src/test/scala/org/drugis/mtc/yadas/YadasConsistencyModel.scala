package org.drugis.mtc.yadas

class YadasConsistencyModelIT extends ConsistencyModelTestBase {
	override def makeModel(nm: NetworkModel[DichotomousMeasurement])
	: ConsistencyModel = {
		new YadasConsistencyModel(nm)
	}
}
