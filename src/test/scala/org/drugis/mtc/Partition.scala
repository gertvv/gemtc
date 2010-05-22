package org.drugis.mtc

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

class PartTest extends ShouldMatchersForJUnit {
	def t(id: String) = new Treatment(id)
	def m(id: String) = new NoneMeasurement(t(id))
	@Test def testPrecondition() {
		intercept[IllegalArgumentException] {
			new Part(t("A"), t("B"), Set[Study[NoneMeasurement]]())
		}
		intercept[IllegalArgumentException] {
			new Part(t("A"), t("B"),
				Set(new Study("1", Map(
					(t("A"), m("A")),
					(t("C"), m("C"))
				))))
		}
		new Part(t("A"), t("B"),
			Set(new Study("1", Map(
				(t("A"), m("A")),
				(t("B"), m("B"))
			)))) should not be (null)
		new Part(t("A"), t("B"),
			Set(new Study("1", Map(
				(t("A"), m("A")),
				(t("B"), m("B")),
				(t("C"), m("C"))
			)))) should not be (null)
	}

	@Test def testTreatments() {
		new Part(t("A"), t("B"),
			Set(new Study("1", Map(
				(t("A"), m("A")),
				(t("B"), m("B")),
				(t("C"), m("C"))
			)))).treatments should be (Set(t("A"), t("B")))
	}

	@Test def testEquals() {
		val map = Map((t("A"), m("A")), (t("B"), m("B")), (t("C"), m("C")))
		val p1 = new Part(t("A"), t("B"),
			Set(new Study("1", map)))
		val p2 = new Part(t("A"), t("C"),
			Set(new Study("1", map)))
		val p3 = new Part(t("A"), t("B"),
			Set(new Study("1", map)))
		val p4 = new Part(t("A"), t("B"),
			Set(new Study("2", map)))

		p1 should be (p1)
		p1 should not be (p2)
		p1 should be (p3)
		p1.hashCode should be (p3.hashCode)
		p1 should not be (p4)
	}
}

class PartitionTest extends ShouldMatchersForJUnit {
	@Test def testPrecondition() {
		fail()
	}

	@Test def testEquals() {
		fail()
	}

	@Test def testReduce() {
		fail()
	}
}
