/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
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

import org.drugis.common.threading.Task
import org.drugis.common.threading.TaskUtil
import org.drugis.common.threading.activity.ActivityTask
import org.drugis.common.threading.ThreadHandler
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

import org.apache.commons.math.stat.descriptive.moment.{Mean, StandardDeviation}

import org.drugis.mtc._
import org.drugis.mtc.ResultsUtil._

class NodeSplitIT extends ShouldMatchersForJUnit {
	val f = 0.15

	def network = {
		val is = classOf[NodeSplitIT].getResourceAsStream("vlaar-longterm.xml")
		Network.dichFromXML(scala.xml.XML.load(is))
	}
	val meanCalc = new Mean()
	val stdDevCalc = new StandardDeviation()

	val ipci = new Treatment("iPCI");
	val mpci = new Treatment("mPCI");
	val spci = new Treatment("sPCI");

	val mDir = -1.01660
	val sDir = 0.3287
	val mInd = -1.16773
	val sInd = 0.4338

	@Test def testResult() {
		val model = new YadasNodeSplitModel(network,
			new BasicParameter(mpci, spci))
		TaskUtil.run(model.getActivityTask)

		val direct = model.getResults.findParameter(new SplitParameter(mpci, spci, true))
		val indirect = model.getResults.findParameter(new SplitParameter(mpci, spci, false))


		mean(model, direct) should be (mDir plusOrMinus f * sDir)
		stdDev(model, direct) should be (sDir plusOrMinus f * sDir)
		mean(model, indirect) should be (mInd plusOrMinus f * sInd)
		stdDev(model, indirect) should be (sInd plusOrMinus f * sInd)
	}

	private def mean(model: MCMCModel, p: Int): Double = { 
		val n = model.getSimulationIterations / 2
		meanCalc.evaluate(getSamples(model.getResults, p, 0), n, n)
	}

	private def stdDev(model: MCMCModel, p: Int): Double = { 
		val n = model.getSimulationIterations / 2
		stdDevCalc.evaluate(getSamples(model.getResults, p, 0), n, n)
	}

}
