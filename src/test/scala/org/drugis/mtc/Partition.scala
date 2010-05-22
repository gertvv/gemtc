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
	def t(id: String) = new Treatment(id)
	def m(id: String) = new NoneMeasurement(t(id))
	val map = Map((t("A"), m("A")), (t("B"), m("B")), (t("C"), m("C")))
	val mapAB = Map((t("A"), m("A")), (t("B"), m("B")))
	val mapBC = Map((t("C"), m("C")), (t("B"), m("B")))
	val p1 = new Part(t("A"), t("A"),
		Set(new Study("1", map)))
	val p2 = new Part(t("A"), t("B"),
		Set(new Study("1", map)))
	val p3 = new Part(t("A"), t("B"),
		Set(new Study("2", map)))
	val p4 = new Part(t("A"), t("C"),
		Set(new Study("2", map)))
	val p5 = new Part(t("B"), t("C"),
		Set(new Study("2", map)))
	val p6 = new Part(t("A"), t("A"),
		Set(new Study("2", map)))
	val p7 = new Part(t("A"), t("B"),
		Set(new Study("1", mapAB), new Study("2", map)))
	val p8 = new Part(t("C"), t("B"),
		Set(new Study("1", mapBC), new Study("2", map)))

	@Test def testPrecondition() {

		new Partition(Set(p1)) should not be (null)


		intercept[IllegalArgumentException] {
			new Partition(Set(p2))
		}

		new Partition(Set(p2, p3)) should not be (null)

		new Partition(Set(p3, p4, p5)) should not be (null)
	}

	@Test def testEquals() {
		new Partition(Set(p1)) should be (new Partition(Set(p1)))
		new Partition(Set(p1)).hashCode should be (
			new Partition(Set(p1)).hashCode)
		new Partition(Set(p1)) should not be (new Partition(Set(p2, p3)))
		new Partition(Set(p1)) should not be (new Partition(Set(p6)))
	}

	@Test def testReduce() {
		new Partition(Set(p3, p4, p5)).reduce should be (new Partition(Set(p6)))

		val pAB = new Part(t("A"), t("B"),
			Set(new Study("1", mapAB),
				new Study("2", map)))
		val pBA = new Part(t("A"), t("B"),
			Set(new Study("2", map)))
		new Partition(Set(p7, p4, p5)).reduce should be (
			new Partition(Set(pAB, pBA)))

		val pBC = new Part(t("B"), t("C"),
			Set(new Study("1", mapBC),
				new Study("2", map)))
		val pCB = new Part(t("B"), t("C"),
			Set(new Study("2", map)))
		new Partition(Set(p3, p8, p4)).reduce should be (
			new Partition(Set(pBC, pCB)))
	}

	@Test def testFromNetwork() {
		val network = Network.noneFromXML(
			<network type="none">
				<treatments>
					<treatment id="A"/>
					<treatment id="B"/>
					<treatment id="C"/>
					<treatment id="D"/>
				</treatments>
				<studies>
					<study id="1">
						<measurement treatment="A" />
						<measurement treatment="D" />
					</study>
					<study id="2">
						<measurement treatment="A" />
						<measurement treatment="B" />
					</study>
					<study id="3">
						<measurement treatment="B" />
						<measurement treatment="C" />
						<measurement treatment="D" />
					</study>
					<study id="4">
						<measurement treatment="A" />
						<measurement treatment="B" />
					</study>
				</studies>
			</network>)
		def t(id: String) = network.treatments.find(t => t.id == id).toList(0)
		def s(id: String) = network.studies.find(s => s.id == id).toList(0)

		val pAB = new Part(t("A"), t("B"), Set(s("2"), s("4")))
		val pBD = new Part(t("B"), t("D"), Set(s("3")))
		val pBC = new Part(t("B"), t("C"), Set(s("3")))
		val pCD = new Part(t("C"), t("D"), Set(s("3")))
		val pAD = new Part(t("A"), t("D"), Set(s("1")))

		val cABDA = new Cycle(List(t("A"), t("B"), t("D"), t("A")))
		val cABCDA = new Cycle(List(t("A"), t("B"), t("C"), t("D"), t("A")))

		Partition(network, cABDA) should be (
			new Partition(Set(pAB, pBD, pAD)))

		Partition(network, cABCDA) should be (
			new Partition(Set(pAB, pBC, pCD, pAD)))

		Partition(network, cABCDA).reduce should be (
			new Partition(Set(pAB, pBD, pAD)))
	}
}
