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

import org.drugis.mtc._

class RelativeEffectArgumentMakerTest extends ShouldMatchersForJUnit {
	val ta = new Treatment("A")
	val tb = new Treatment("B")
	val tc = new Treatment("C")
	val td = new Treatment("D")
	val treatmentList = List(ta, tb, tc, td)

	def makeM(t: Treatment): DichotomousMeasurement = {
		new DichotomousMeasurement(t, 50, 100)
	}

	val study1 = new Study[DichotomousMeasurement]("1", Map[Treatment, DichotomousMeasurement](
		(ta, makeM(ta)), (tb, makeM(tb))))
	val study2 = new Study[DichotomousMeasurement]("2", Map[Treatment, DichotomousMeasurement](
		(tb, makeM(tb)), (tc, makeM(tc)), (td, makeM(td))))
	val study3 = new Study[DichotomousMeasurement]("3", Map[Treatment, DichotomousMeasurement](
		(ta, makeM(ta)), (td, makeM(td))))
	val study4 = new Study[DichotomousMeasurement]("4", Map[Treatment, DichotomousMeasurement](
		(tc, makeM(tc)), (td, makeM(td))))
	val studyList = List(study1, study2, study3, study4)

	val network = new Network(
		Set[Treatment]() ++ treatmentList,
		Set[Study[DichotomousMeasurement]]() ++ studyList)

	val spanningTree = new Tree[Treatment](
		Set((ta, tb), (tb, tc), (ta, td)), ta)

	val networkModel = new NetworkModel(
		new InconsistencyParametrization(network,
		new FundamentalGraphBasis(network.treatmentGraph, spanningTree)),
		Map[Study[DichotomousMeasurement], Treatment](
			(study1, ta), (study2, tb), (study3, ta), (study4, tc)),
		treatmentList, studyList)

	@Test def testGetArgumentConsistency() {
		val input = Array(-1.0, 1.0, 3.0)
		val expected1 = List(-1.0)
		val expected2 = List(3.0, 2.0)
		val expected3 = List(1.0)
		val expected4 = List(-1.0)

		val maker1a = new RelativeEffectArgumentMaker(networkModel, 0, None,
			study1)
		maker1a.getArgument(Array(input)).toList should be (expected1)

		val maker1b = new RelativeEffectArgumentMaker(networkModel, 1, None,
			study1)
		maker1b.getArgument(Array(null, input)).toList should be (expected1)

		val maker2a = new RelativeEffectArgumentMaker(networkModel, 0, None,
			study2)
		maker2a.getArgument(Array(input)).toList should be (expected2)

		val maker3a = new RelativeEffectArgumentMaker(networkModel, 0, None,
			study3)
		maker3a.getArgument(Array(input)).toList should be (expected3)

		val maker4a = new RelativeEffectArgumentMaker(networkModel, 0, None,
			study4)
		maker4a.getArgument(Array(input)).toList should be (expected4)
	}

	@Test def testGetArgumentInconsistency() {
		val basic = Array(-1.0, 1.0, 3.0) // AB, AD, BC
		val incons = Array(-0.5, 0.5) // ABCDA, ABDA
		val expected1 = List(-1.0)
		val expected2 = List(3.0, 2.5)
		val expected3 = List(1.0)
		val expected4 = List(-1.5)

		val maker1a = new RelativeEffectArgumentMaker(networkModel, 0, Some(1),
			study1)
		maker1a.getArgument(Array(basic, incons)).toList should be (expected1)

		val maker2a = new RelativeEffectArgumentMaker(networkModel, 0, Some(1),
			study2)
		maker2a.getArgument(Array(basic, incons)).toList should be (expected2)

		val maker3a = new RelativeEffectArgumentMaker(networkModel, 0, Some(1),
			study3)
		maker3a.getArgument(Array(basic, incons)).toList should be (expected3)

		val maker4a = new RelativeEffectArgumentMaker(networkModel, 0, Some(1),
			study4)
		maker4a.getArgument(Array(basic, incons)).toList should be (expected4)
	}

	/**
	 * Created because creating an inconsistency model with no inconsistencies
	 * threw an exception.
	 */
	@Test def testGetArgumentInconsistencyNone() {
		val simpleNet = Network.dichFromXML(
			<network>
				<treatments>
					<treatment id="A" />
					<treatment id="B" />
				</treatments>
				<studies>	
					<study id="01">
						<measurement treatment="A" responders="0" sample="1"/>
						<measurement treatment="B" responders="0" sample="1"/>
					</study>
				</studies>
			</network>)
		val simpleModel = InconsistencyNetworkModel(simpleNet)

		val basic = Array(-1.0)
		val incons: Array[Double] = Array()

		val maker0 = new RelativeEffectArgumentMaker(simpleModel, 0, Some(1), simpleModel.studyList(0))
		maker0.getArgument(Array(basic, incons)).toList should be (basic.toList)
	}
}
