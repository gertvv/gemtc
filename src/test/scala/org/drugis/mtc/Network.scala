package org.drugis.mtc

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before
import org.junit.Ignore

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
					<measurement treatment="A" responders="1" sample="100" />
					<measurement treatment="B" responders="1" sample="100" />
				</study>
				<study id="2">
					<measurement treatment="A" responders="1" sample="100" />
					<measurement treatment="B" responders="1" sample="100" />
					<measurement treatment="C" responders="1" sample="100" />
				</study>
				<study id="3">
					<measurement treatment="D" responders="1" sample="100" />
					<measurement treatment="B" responders="1" sample="100" />
				</study>
			</studies>
		</network>

	@Test def testFromXMLDich() {
		val network = Network.dichFromXML(networkXML)

		val a = new DichotomousMeasurement(new Treatment("A"), 1, 100)
		val b = new DichotomousMeasurement(new Treatment("B"), 1, 100)
		val c = new DichotomousMeasurement(new Treatment("C"), 1, 100)
		val d = new DichotomousMeasurement(new Treatment("D"), 1, 100)

		val s1 = new Study[DichotomousMeasurement]("1", Map((a.treatment, a), (b.treatment, b)))
		val s2 = new Study[DichotomousMeasurement]("2", Map((a.treatment, a), (b.treatment, b), 
			(c.treatment, c)))
		val s3 = new Study[DichotomousMeasurement]("3", Map((b.treatment, b), (d.treatment, d)))

		network.treatments should be (Set[Treatment](
			a.treatment, b.treatment, c.treatment, d.treatment))
		network.studies should be (Set[Study[DichotomousMeasurement]](s1, s2, s3))
		network.measurementType should be (classOf[DichotomousMeasurement])
	}

	@Test def testFromXMLCont() {
		val contXML =
			<network type="continuous">
				<treatments>
					<treatment id="A"/>
					<treatment id="B"/>
				</treatments>
				<studies>
					<study id="1">
						<measurement treatment="A" mean="1.2" standardDeviation="0.25" sample="100" />
						<measurement treatment="B" mean="1.8" standardDeviation="0.8" sample="100" />
					</study>
				</studies>
			</network>
		val network = Network.contFromXML(contXML)

		val a = new ContinuousMeasurement(new Treatment("A"), 1.2, 0.25, 100)
		val b = new ContinuousMeasurement(new Treatment("B"), 1.8, 0.8,  100)

		val s1 = new Study[ContinuousMeasurement]("1", Map((a.treatment, a), (b.treatment, b)))

		network.treatments should be (Set[Treatment](
			a.treatment, b.treatment))
		network.studies should be (Set[Study[ContinuousMeasurement]](s1))

		network.measurementType should be (classOf[ContinuousMeasurement])
	}

	@Ignore
	@Test def testFromXMLGeneric() {
		fail()
	}

	@Test def testFilterTreatments() {
		val ts = Set(
			new Treatment("A"),
			new Treatment("B"),
			new Treatment("C"))

		val network = Network.dichFromXML(networkXML).filterTreatments(ts)

		val a = new DichotomousMeasurement(new Treatment("A"), 1, 100)
		val b = new DichotomousMeasurement(new Treatment("B"), 1, 100)
		val c = new DichotomousMeasurement(new Treatment("C"), 1, 100)

		val s1 = new Study[DichotomousMeasurement]("1", Map((a.treatment, a), (b.treatment, b)))
		val s2 = new Study[DichotomousMeasurement]("2", Map((a.treatment, a), (b.treatment, b), 
			(c.treatment, c)))

		network.treatments should be (Set[Treatment](
			a.treatment, b.treatment, c.treatment))
		network.studies should be (Set[Study[DichotomousMeasurement]](s1, s2))
	}

	@Test def testFilterTreatmentsFilterStudy() {
		val ts = Set(
			new Treatment("A"),
			new Treatment("B"),
			new Treatment("D"))

		val network = Network.dichFromXML(networkXML).filterTreatments(ts)

		val a = new DichotomousMeasurement(new Treatment("A"), 1, 100)
		val b = new DichotomousMeasurement(new Treatment("B"), 1, 100)
		val d = new DichotomousMeasurement(new Treatment("D"), 1, 100)

		val s1 = new Study[DichotomousMeasurement]("1", Map((a.treatment, a), (b.treatment, b)))
		val s2 = new Study[DichotomousMeasurement]("2", Map((a.treatment, a), (b.treatment, b)))
		val s3 = new Study[DichotomousMeasurement]("3", Map((b.treatment, b), (d.treatment, d)))

		network.treatments should be (Set[Treatment](
			a.treatment, b.treatment, d.treatment))
		network.studies should be (Set[Study[DichotomousMeasurement]](s1, s2, s3))
	}

	@Test def testTreatmentGraph() {
		val network = Network.dichFromXML(networkXML)

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
		val network = Network.dichFromXML(networkXML)

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
		val network = Network.dichFromXML(networkXML)

		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")
		val d = new Treatment("D")

		network.edgeVector should be (List[(Treatment, Treatment)](
			(a, b), (a, c), (b, c), (b, d)
		))
	}

	@Test def testEvidenceMatrix() {
		val network = Network.dichFromXML(networkXML)

		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")

		val cycle = new UndirectedGraph[Treatment](
			Set[(Treatment, Treatment)]((a, b), (a, c), (b, c)))

		val m1 = new Matrix[Boolean](List[List[Boolean]](
			List[Boolean](true, false, false),
			List[Boolean](true, true, true)
		))
		val m2 = m1.exchangeRows(0, 1)

		val evidence = network.evidenceMatrix(cycle)
		assert(evidence == m1 || evidence == m2)

		network.evidenceDimensionality(cycle) should be (2)
		assert(!network.isInconsistency(cycle))
	}

	@Test def testInconsistency() {
		val network = Network.dichFromXML(
			<network>
				<treatments>
					<treatment id="A"/>
					<treatment id="B"/>
					<treatment id="C"/>
				</treatments>
				<studies>
					<study id="1">
						<measurement treatment="A" responders="1" sample="100" />
						<measurement treatment="B" responders="1" sample="100" />
					</study>
					<study id="2">
						<measurement treatment="A" responders="1" sample="100" />
						<measurement treatment="B" responders="1" sample="100" />
						<measurement treatment="C" responders="1" sample="100" />
					</study>
					<study id="3">
						<measurement treatment="B" responders="1" sample="100" />
						<measurement treatment="C" responders="1" sample="100" />
					</study>
					<study id="4">
						<measurement treatment="A" responders="1" sample="100" />
						<measurement treatment="C" responders="1" sample="100" />
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
		val network = Network.dichFromXML(
			<network>
				<treatments>
					<treatment id="A"/>
					<treatment id="B"/>
					<treatment id="C"/>
				</treatments>
				<studies>
					<study id="1">
						<measurement treatment="A" responders="1" sample="100" />
						<measurement treatment="B" responders="1" sample="100" />
					</study>
					<study id="2">
						<measurement treatment="A" responders="1" sample="100" />
						<measurement treatment="B" responders="1" sample="100" />
						<measurement treatment="C" responders="1" sample="100" />
					</study>
					<study id="3">
						<measurement treatment="B" responders="1" sample="100" />
						<measurement treatment="C" responders="1" sample="100" />
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
		val network = Network.dichFromXML(<network>
			<treatments>
				<treatment id="A"/>
				<treatment id="B"/>
				<treatment id="C"/>
				<treatment id="D"/>
			</treatments>
			<studies>
				<study id="1">
					<measurement treatment="D" responders="1" sample="100" />
					<measurement treatment="B" responders="1" sample="100" />
					<measurement treatment="C" responders="1" sample="100" />
				</study>
				<study id="2">
					<measurement treatment="A" responders="1" sample="100" />
					<measurement treatment="B" responders="1" sample="100" />
				</study>
				<study id="3">
					<measurement treatment="A" responders="1" sample="100" />
					<measurement treatment="C" responders="1" sample="100" />
				</study>
				<study id="4">
					<measurement treatment="A" responders="1" sample="100" />
					<measurement treatment="D" responders="1" sample="100" />
				</study>
			</studies>
		</network>)

		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")
		val d = new Treatment("D")

		network.countInconsistencies(
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, b), (a, c), (a, d)), a)) should be (3)

		network.countInconsistencies(
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, b), (b, d), (b, c)), a)) should be (2)

		network.countInconsistencies(
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, c), (a, d), (d, b)), a)) should be (3)

		network.countInconsistencies(
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, d), (d, c), (d, b)), a)) should be (2)
	}
}
