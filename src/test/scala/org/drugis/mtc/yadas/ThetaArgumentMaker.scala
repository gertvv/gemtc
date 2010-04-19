package org.drugis.mtc.yadas

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

import org.drugis.mtc._

class ThetaArgumentMakerTest extends ShouldMatchersForJUnit {
	val ta = new Treatment("A")
	val tb = new Treatment("B")
	val tc = new Treatment("C")
	val td = new Treatment("D")
	val treatmentList = List(ta, tb, tc, td)

	val study1 = new Study[DichotomousMeasurement]("1", Map[Treatment, DichotomousMeasurement](
		(ta, new DichotomousMeasurement(ta, 0, 1)), (tb, new DichotomousMeasurement(tb, 0, 1))))
	val study2 = new Study[DichotomousMeasurement]("2", Map[Treatment, DichotomousMeasurement](
		(tb, new DichotomousMeasurement(tb, 0, 1)), (tc, new DichotomousMeasurement(tc, 0, 1)), (td, new DichotomousMeasurement(td, 0, 1))))
	val study3 = new Study[DichotomousMeasurement]("3", Map[Treatment, DichotomousMeasurement](
		(ta, new DichotomousMeasurement(ta, 0, 1)), (td, new DichotomousMeasurement(td, 0, 1))))
	val study4 = new Study[DichotomousMeasurement]("4", Map[Treatment, DichotomousMeasurement](
		(tc, new DichotomousMeasurement(tc, 0, 1)), (td, new DichotomousMeasurement(td, 0, 1))))
	val studyList = List(study1, study2, study3, study4)

	val network = new Network(
		Set[Treatment]() ++ treatmentList,
		Set[Study[DichotomousMeasurement]]() ++ studyList)

	val spanningTree = new Tree[Treatment](
		Set((ta, tb), (tb, tc), (ta, td)), ta)

	val networkModel = new NetworkModel(network,
		new FundamentalGraphBasis(network.treatmentGraph, spanningTree),
		Map[Study[DichotomousMeasurement], Treatment](
			(study1, ta), (study2, tb), (study3, ta), (study4, tc)),
		treatmentList, studyList)

	@Test def testGetArgument() {
		val baselines = Array(1.0, 2.0, 1.5, 3.0)
		val deltas = Array(-1.0, 3.0, 2.0, 1.0, -1.0)
		val expected = List(1.0, 0.0, 2.0, 5.0, 4.0, 1.5, 2.5, 3.0, 2.0)
		
		val maker0 = new ThetaArgumentMaker(networkModel, 0, 1)
		maker0.getArgument(Array(baselines, deltas)).toList should be (expected)
	}
}
