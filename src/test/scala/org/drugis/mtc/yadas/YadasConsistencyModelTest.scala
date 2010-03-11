package org.drugis.mtc.yadas

class YadasConsistencyModelTest extends ConsistencyModelTestBase {
	override def makeModel(nm: NetworkModel): ConsistencyModel = {
		new YadasConsistencyModel(nm)
	}
}
