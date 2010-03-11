package org.drugis.mtc.yadas

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

import org.drugis.mtc._

class RealEffectArgumentMakerTest extends ShouldMatchersForJUnit {
	val ta = new Treatment("A")
	val tb = new Treatment("B")
	val tc = new Treatment("C")
	val td = new Treatment("D")
	val treatmentList = List(ta, tb, tc, td)

	val study1 = new Study("1", Map[Treatment, Measurement](
		(ta, new Measurement(ta, 0, 1)), (tb, new Measurement(tb, 0, 1))))
	val study2 = new Study("2", Map[Treatment, Measurement](
		(tb, new Measurement(tb, 0, 1)), (tc, new Measurement(tc, 0, 1)), (td, new Measurement(td, 0, 1))))
	val study3 = new Study("3", Map[Treatment, Measurement](
		(ta, new Measurement(ta, 0, 1)), (td, new Measurement(td, 0, 1))))
	val study4 = new Study("4", Map[Treatment, Measurement](
		(tc, new Measurement(tc, 0, 1)), (td, new Measurement(td, 0, 1))))
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

	def ilogit(x: Double) = 1 / (1 + Math.exp(-x))

	@Test def testGetArgument() {
		val baselines = Array(1.0, 2.0, 1.5, 3.0)
		val deltas = Array(-1.0, 3.0, 2.0, 1.0, -1.0)
		val thetas = Array(1.0, 0.0, 2.0, 5.0, 4.0, 1.5, 2.5, 3.0, 2.0)
		val expected = thetas.toList

		
		val maker0 = new RealEffectArgumentMaker(networkModel, 0, 1)
		maker0.getArgument(Array(baselines, deltas)).toList should be (expected)
	}
}
