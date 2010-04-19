package org.drugis.mtc.yadas

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test

import org.drugis.mtc._

class YadasConsistencyModelTest extends ShouldMatchersForJUnit {
	val model = new YadasConsistencyModel(NetworkModel(Network.dichFromXML(
		<network>
			<treatments>
				<treatment id="A"/>
				<treatment id="B"/>
			</treatments>
			<studies>
				<study id="1">
					<measurement treatment="A" responders="1" sample="100" />
					<measurement treatment="B" responders="1" sample="100" />
				</study>
			</studies>
		</network>)))

	@Test def testSimulationIterations() {
		model.getSimulationIterations() should be (100000)
		model.setSimulationIterations(10000)
		model.getSimulationIterations() should be (10000)
		intercept[IllegalArgumentException] {
			model.setSimulationIterations(10001)
		}
		intercept[IllegalArgumentException] {
			model.setSimulationIterations(0)
		}
	}


	@Test def testBurnInIterations() {
		model.getBurnInIterations() should be (20000)
		model.setBurnInIterations(10000)
		model.getBurnInIterations() should be (10000)
		intercept[IllegalArgumentException] {
			model.setBurnInIterations(10001)
		}
		intercept[IllegalArgumentException] {
			model.setBurnInIterations(0)
		}
	}
}

class YadasConsistencyModelIT extends ConsistencyModelTestBase {
	override def makeModel(nm: NetworkModel[DichotomousMeasurement])
	: ConsistencyModel = {
		new YadasConsistencyModel(nm)
	}
}
