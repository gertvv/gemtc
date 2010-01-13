package org.drugis

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

class StudyTest extends ShouldMatchersForJUnit {
	@Test def testEquals() {
		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")

		new Study("1", Set[Treatment](a, b)) should be (
			new Study("1", Set[Treatment](a, b)))
	}
}
