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

	val study1 = new Study("1", Map[Treatment, Measurement](
		(ta, null), (tb, null)))
	val study2 = new Study("2", Map[Treatment, Measurement](
		(tb, null), (tc, null), (td, null)))
	val study3 = new Study("3", Map[Treatment, Measurement](
		(ta, null), (td, null)))
	val study4 = new Study("4", Map[Treatment, Measurement](
		(tc, null), (td, null)))
	val studyList = List(study1, study2, study3, study4)

	val network = new Network(
		Set[Treatment]() ++ treatmentList,
		Set[Study]() ++ studyList)

	val spanningTree = new Tree[Treatment](
		Set((ta, tb), (tb, tc), (ta, td)), ta)

	val networkModel = new NetworkModel(network,
		new FundamentalGraphBasis(network.treatmentGraph, spanningTree),
		Map[Study, Treatment](
			(study1, ta), (study2, tb), (study3, ta), (study4, tc)),
		treatmentList, studyList)

	@Test def testGetArgumentConsistency() {
		val input = Array(-1.0, 1.0, 3.0)
		val expected = List(-1.0, 3.0, 2.0, 1.0, -1.0)

		val maker0 = new RelativeEffectArgumentMaker(networkModel, 0, None)
		maker0.getArgument(Array(input)).toList should be (expected)

		val maker1 = new RelativeEffectArgumentMaker(networkModel, 1, None)
		maker1.getArgument(Array(null, input)).toList should be (expected)
	}

	@Test def testGetArgumentInconsistency() {
		val basic = Array(-1.0, 1.0, 3.0)
		val incons = Array(0.5, -0.5)
		val expected = List(-1.0, 3.0, 2.5, 1.0, -1.5)

		val maker0 = new RelativeEffectArgumentMaker(networkModel, 0, Some(1))
		maker0.getArgument(Array(basic, incons)).toList should be (expected)
	}
}
