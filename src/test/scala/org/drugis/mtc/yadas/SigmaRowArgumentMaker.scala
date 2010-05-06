package org.drugis.mtc.yadas

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

import org.drugis.mtc._

class SigmaRowArgumentMakerTest extends ShouldMatchersForJUnit {
	val ta = new Treatment("A")
	val tb = new Treatment("B")
	val tc = new Treatment("C")
	val td = new Treatment("D")

	private def meas(t: Treatment) = new DichotomousMeasurement(t, 0, 1)

	val study1 = new Study[DichotomousMeasurement]("1",
		Map[Treatment, DichotomousMeasurement](
		(tb, meas(tb)), (tc, meas(tc)), (ta, meas(ta))))

	val study2 = new Study[DichotomousMeasurement]("2",
		Map[Treatment, DichotomousMeasurement](
		(tb, meas(tb)), (tc, meas(tc)), (ta, meas(ta)), (td, meas(td))))

	@Test def testGetArgument1() {
		val sigma = Array(2.0)
		val expected0 = List(4.0, 2.0)
		val expected1 = List(2.0, 4.0)
		
		val maker0 = new SigmaRowArgumentMaker(study1, 0, 0)
		maker0.getArgument(Array(sigma)).toList should be (expected0)

		val maker1 = new SigmaRowArgumentMaker(study1, 0, 1)
		maker1.getArgument(Array(sigma)).toList should be (expected1)
	}

	@Test def testGetArgument2() {
		val sigma = Array(2.0)
		val expected0 = List(4.0, 2.0, 2.0)
		val expected1 = List(2.0, 4.0, 2.0)
		
		val maker0 = new SigmaRowArgumentMaker(study2, 0, 0)
		maker0.getArgument(Array(sigma)).toList should be (expected0)

		val maker1 = new SigmaRowArgumentMaker(study2, 0, 1)
		maker1.getArgument(Array(sigma)).toList should be (expected1)
	}
}
