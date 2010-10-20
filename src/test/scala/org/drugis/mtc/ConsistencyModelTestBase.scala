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

package org.drugis.mtc

import org.drugis.common.threading.Task
import org.drugis.common.threading.activity.ActivityTask
import org.drugis.common.threading.ThreadHandler
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

abstract class ConsistencyModelTestBase extends ShouldMatchersForJUnit {
	val f = 0.05
	def network = Network.dichFromXML(
		<network description="Smoking cessation rates">
			<treatments>
				<treatment id="A">No Contact</treatment>
				<treatment id="B">Self-help</treatment>
				<treatment id="C">Individual Counseling</treatment>
			</treatments>
			<studies>
				<study id="01">
					<measurement treatment="A" responders="9" sample="140" />
					<measurement treatment="B" responders="23" sample="140" />
					<measurement treatment="C" responders="10" sample="138" />
				</study>
				<study id="02">
					<measurement treatment="A" responders="79" sample="702" />
					<measurement treatment="B" responders="77" sample="694" />
				</study>
				<study id="03">
					<measurement treatment="A" responders="18" sample="671" />
					<measurement treatment="C" responders="21" sample="535" />
				</study>
			</studies>
		</network>)

	val ta = new Treatment("A")
	val tb = new Treatment("B")
	val tc = new Treatment("C")

	val spanningTree = new Tree[Treatment](
		Set((ta, tb), (tb, tc)), ta)

	def makeModel(nm: NetworkModel[DichotomousMeasurement, ConsistencyParametrization[DichotomousMeasurement]]): ConsistencyModel

	var model: ConsistencyModel = null 

	@Before def setUp() {
		model = makeModel(ConsistencyNetworkModel(network, spanningTree))
	}

	@Test def testIsNotReady() {
		model.isReady should be (false)
	}

	@Test def testBasicParameters() {
		run(model)
		model.isReady should be (true)

		val m = model
		val dAB = m.getRelativeEffect(ta, tb)
		val dBC = m.getRelativeEffect(tb, tc)

		// Values below obtained via a run through regular JAGS with 30k/20k
		// iterations. Taking .15 sd as acceptable margin (same as JAGS does
		// for testing against WinBUGS results).
		val mAB = 0.530409
		val sAB = 0.7925273
		dAB.getMean should be (mAB plusOrMinus f * sAB)
		dAB.getStandardDeviation should be(sAB plusOrMinus f * sAB)
		val mBC = -0.4314525
		val sBC = 0.951072
		dBC.getMean should be (mBC plusOrMinus f * sBC)
		dBC.getStandardDeviation should be(sBC plusOrMinus f * sBC)
	}

	@Test def testDerivedParameters() {
		run(model)
		model.isReady should be (true)

		val m = model

		val dBA = m.getRelativeEffect(tb, ta)
		dBA should not be (null)
		val dAC = m.getRelativeEffect(ta, tc)
		val mAC = 0.09895649
		val sAC = 0.7997789
		dAC.getMean should be (mAC plusOrMinus f * sAC)
		dAC.getStandardDeviation should be(sAC plusOrMinus f * sAC)
	}
	
	def run(model: MCMCModel) {
		val th = ThreadHandler.getInstance()
		val task = model.getActivityTask()
		th.scheduleTask(task)
		waitUntilReady(task)
	}
	
	def waitUntilReady(task: Task) {
		while (!task.isFinished()) {
			Thread.sleep(100);
		}
	}
}
