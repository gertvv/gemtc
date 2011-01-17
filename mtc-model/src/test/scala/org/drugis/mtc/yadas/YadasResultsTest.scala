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

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

import org.drugis.mtc.{Parameter, BasicParameter, Treatment,
	MCMCResultsListener, MCMCResultsEvent}
import gov.lanl.yadas.MCMCParameter

class YadasResultsTest extends ShouldMatchersForJUnit {
	private var results: YadasResults = null
	private val param1 = new BasicParameter(
		new Treatment("a"),
		new Treatment("b"))
	private val param2 = new BasicParameter(
		new Treatment("b"),
		new Treatment("c"))
	private val param3 = new BasicParameter(
		new Treatment("a"),
		new Treatment("c"))
	private val deriv  = new Derivation(
		Map[Parameter, Int]((param1, 1), (param2, 1)))

	private val paramx = new BasicParameter(
		new Treatment("a"),
		new Treatment("x"))

	@Before def setUp() {
		results = new YadasResults()
		results.setNumberOfChains(1)
		results.setNumberOfIterations(10)
		results.setDirectParameters(List(param1, param2))
		results.setDerivedParameters(List((param3, deriv)))
	}

	@Test def testGetters() {
		results.getParameters.toList should be (List(param1, param2))
		results.findParameter(param1) should be (0)
		results.findParameter(param2) should be (1)
		results.findParameter(paramx) should be (-1)
		results.findParameter(param3) should be (2)
		results.getNumberOfChains should be (1)
		results.getNumberOfSamples should be (0)
		results.simulationFinished()
		results.getNumberOfSamples should be (10)
		results.getSample(0, 0, 0) should be (0.0)
		results.getSamples(0, 0).toList should be ((0 until 10).map(x => 0.0))
		intercept[IndexOutOfBoundsException] { results.getSample(0, 0, 10) }
		intercept[IndexOutOfBoundsException] { results.getSample(0, 1, 3) }
		intercept[IndexOutOfBoundsException] { results.getSample(3, 0, 3) }
		intercept[IndexOutOfBoundsException] { results.getSamples(0, 1) }
		intercept[IndexOutOfBoundsException] { results.getSamples(3, 0) }
	}

	@Test def testWriters() {
		val mcmcParam = new MCMCParameter(
			Array[Double](0.0, 1.0),
			Array[Double](0.1, 0.1), "name")
		val writer = results.getParameterWriter(param2, 0, mcmcParam, 1)
		for (i <- (0 until 10)) {
			mcmcParam.setValue(Array[Double](0.0, i))
			writer.output()
		}
		results.simulationFinished()
		results.getSamples(1, 0).toList should be ((0 until 10).map(
			x => x.toDouble))
	}

	@Test def testAdditionalIterationsPreservesSamples() {
		val mcmcParam = new MCMCParameter(
			Array[Double](0.0, 1.0),
			Array[Double](0.1, 0.1), "name")
		val writer = results.getParameterWriter(param2, 0, mcmcParam, 1)
		for (i <- (0 until 10)) {
			mcmcParam.setValue(Array[Double](0.0, i))
			writer.output()
		}
		results.simulationFinished()

		val firstPart = (0 until 10).map(x => x.toDouble)

		results.setNumberOfIterations(20)
		results.simulationFinished()
		val secondPart = (0 until 10).map(x => 0.0)

		results.getSamples(1, 0).toList should be (
			(firstPart ++ secondPart).toList)
	}

	@Test def testDerivedSamples() {
		val mcmcParam = new MCMCParameter(
			Array[Double](0.0, 1.0),
			Array[Double](0.1, 0.1), "name")
		val writer1 = results.getParameterWriter(param1, 0, mcmcParam, 0)
		val writer2 = results.getParameterWriter(param2, 0, mcmcParam, 1)
		for (i <- (0 until 10)) {
			mcmcParam.setValue(Array[Double](i + 2, i))
			writer1.output()
			writer2.output()
		}
		results.simulationFinished()

		val expected = (0 until 10).map(i => 2 * (i + 1))
		results.getSamples(2, 0).toList should be (expected.toList)
	}

	@Test def testEvent() {
		var received = false
		results.addResultsListener(new MCMCResultsListener() {
			def resultsEvent(evt: MCMCResultsEvent) {
				received = true;
			}
		})
		results.simulationFinished()
		received should be (true)
	}
}
