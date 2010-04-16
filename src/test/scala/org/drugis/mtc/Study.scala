package org.drugis.mtc

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

class StudyTest extends ShouldMatchersForJUnit {
	@Test def testEquals() {
		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")

		val meas = Map[Treatment, Measurement](
				(a, new DichotomousMeasurement(a, 10, 100)),
				(b, new DichotomousMeasurement(b, 10, 100))
			)

		new Study("1", meas) should be (new Study("1", meas))
	}
}
