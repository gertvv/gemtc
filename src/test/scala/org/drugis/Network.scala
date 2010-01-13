package org.drugis

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

class NetworkTest extends ShouldMatchersForJUnit {
	val networkXML =
		<network>
			<treatments>
				<treatment id="A"/>
				<treatment id="B"/>
				<treatment id="C"/>
				<treatment id="D"/>
			</treatments>
			<studies>
				<study id="1">
					<measurement treatment="A"/>
					<measurement treatment="B"/>
				</study>
				<study id="2">
					<measurement treatment="A"/>
					<measurement treatment="B"/>
					<measurement treatment="C"/>
				</study>
				<study id="3">
					<measurement treatment="D"/>
					<measurement treatment="B"/>
				</study>
			</studies>
		</network>

	@Test def testFromXML() {
		val network = Network.fromXML(networkXML)

		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")
		val d = new Treatment("D")

		val s1 = new Study("1", Set[Treatment](a, b))
		val s2 = new Study("2", Set[Treatment](a, b, c))
		val s3 = new Study("3", Set[Treatment](b, d))

		network.treatments should be (Set[Treatment](a, b, c, d))
		network.studies should be (Set[Study](s1, s2, s3))
	}

	@Test def testTreatmentGraph() {
		val network = Network.fromXML(networkXML)

		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")
		val d = new Treatment("D")

		network.treatmentGraph should be (new UndirectedGraph[Treatment](
			Set[(Treatment, Treatment)](
				(a, b), (a, c), (b, c), (b, d)
			)
		))
	}

	@Test def testSupportingEvidence() {
		val network = Network.fromXML(networkXML)

		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")

		val cycle = new UndirectedGraph[Treatment](
			Set[(Treatment, Treatment)]((a, b), (a, c), (b, c)))

		network.supportingEvidence(cycle) should be (
			Set[UndirectedGraph[Treatment]](
				new UndirectedGraph[Treatment](
					Set[(Treatment, Treatment)]((a, b))),
				new UndirectedGraph[Treatment](
					Set[(Treatment, Treatment)]((a, b), (a, c), (b, c)))
			)
		)
	}

	@Test def testEdgeVector() {
		val network = Network.fromXML(networkXML)

		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")
		val d = new Treatment("D")

		network.edgeVector should be (List[(Treatment, Treatment)](
			(a, b), (a, c), (b, c), (b, d)
		))
	}

	@Test def testEvidenceMatrix() {
		val network = Network.fromXML(networkXML)

		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")

		val cycle = new UndirectedGraph[Treatment](
			Set[(Treatment, Treatment)]((a, b), (a, c), (b, c)))

		val m1 = new Matrix[Boolean](List[List[Boolean]](
			List[Boolean](true, false, false),
			List[Boolean](true, true, true),
		))
		val m2 = m1.exchangeRows(0, 1)

		val evidence = network.evidenceMatrix(cycle)
		assert(evidence == m1 || evidence == m2)

		network.evidenceDimensionality(cycle) should be (2)
		assert(!network.isInconsistency(cycle))
	}

	@Test def testInconsistency() {
		val network = Network.fromXML(
			<network>
				<treatments>
					<treatment id="A"/>
					<treatment id="B"/>
					<treatment id="C"/>
				</treatments>
				<studies>
					<study id="1">
						<measurement treatment="A"/>
						<measurement treatment="B"/>
					</study>
					<study id="2">
						<measurement treatment="A"/>
						<measurement treatment="B"/>
						<measurement treatment="C"/>
					</study>
					<study id="3">
						<measurement treatment="B"/>
						<measurement treatment="C"/>
					</study>
					<study id="4">
						<measurement treatment="A"/>
						<measurement treatment="C"/>
					</study>
				</studies>
			</network>)

		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")

		val cycle = new UndirectedGraph[Treatment](
			Set[(Treatment, Treatment)]((a, b), (a, c), (b, c)))

		network.evidenceMatrix(cycle).nRows should be (4)
		network.evidenceDimensionality(cycle) should be (3)
		assert(network.isInconsistency(cycle))
	}

	@Test def testCountInconsistencies() {
		val network = Network.fromXML(
			<network>
				<treatments>
					<treatment id="A"/>
					<treatment id="B"/>
					<treatment id="C"/>
				</treatments>
				<studies>
					<study id="1">
						<measurement treatment="A"/>
						<measurement treatment="B"/>
					</study>
					<study id="2">
						<measurement treatment="A"/>
						<measurement treatment="B"/>
						<measurement treatment="C"/>
					</study>
					<study id="3">
						<measurement treatment="B"/>
						<measurement treatment="C"/>
					</study>
				</studies>
			</network>)

		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")

		val st = new Tree[Treatment](Set[(Treatment, Treatment)](
			(a, b), (a, c)), a)

		network.countInconsistencies(st) should be (1)
	}

	@Test def testCountInconsistencies2() {
		fail()
	}
}
