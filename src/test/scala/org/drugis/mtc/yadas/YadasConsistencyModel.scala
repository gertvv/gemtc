/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2010 Gert van Valkenhoef.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
	override def makeModel(nm: NetworkModel[DichotomousMeasurement, InconsistencyParametrization[DichotomousMeasurement]])
	: ConsistencyModel = {
		new YadasConsistencyModel(nm)
	}
}
