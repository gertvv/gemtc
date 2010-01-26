package org.drugis

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

class NetworkModelTest extends ShouldMatchersForJUnit {
	val network = Network.fromXML(<network>
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
					<measurement treatment="C" responders="1" sample="100" />
				</study>
				<study id="2">
					<measurement treatment="B" responders="1" sample="100" />
					<measurement treatment="C" responders="1" sample="100" />
					<measurement treatment="D" responders="1" sample="100" />
				</study>
				<study id="3">
					<measurement treatment="A" responders="1" sample="100" />
					<measurement treatment="C" responders="1" sample="100" />
				</study>
			</studies>
		</network>)

	val ta = new Treatment("A")
	val tb = new Treatment("B")
	val tc = new Treatment("C")
	val td = new Treatment("D")
	val studies = network.studies.toList.sort((a, b) => a.id < b.id)

	val spanningTree = new Tree[Treatment](
		Set((ta, tc), (ta, tb), (tb, td)), ta)

	@Test def testAssignBaselines() {
		val baselines = NetworkModel.assignBaselines(network, spanningTree)
		baselines(studies(0)) should be (tb)
		baselines(studies(1)) should be (td)
		baselines(studies(2)) should be (ta)
	}

	@Test def testStudyList() {
		NetworkModel.studyList(network.studies) should be (studies)
	}

	@Test def testTreatmentList() {
		NetworkModel.treatmentList(network.treatments) should be (
			List(ta, tb, tc, td))
	}

	@Test def testIndexMap() {
		val map = NetworkModel.indexMap(studies)
		for (i <- 0 until studies.length) {
			map(studies(i)) should be (i + 1)
		}
	}

	@Test def testFactory() {
		val model = NetworkModel(network, spanningTree)
		model.studyBaseline(studies(0)) should be (tb)
		model.studyBaseline(studies(1)) should be (td)
		model.studyBaseline(studies(2)) should be (ta)
		model.treatmentList should be (List(ta, tb, tc, td))
		model.studyList should be (studies)
		model.basis.tree should be (spanningTree)
		model.basis.graph should be (network.treatmentGraph)
	}

	@Test def testData() {
		val data = NetworkModel(network, spanningTree).data
		data.size should be (8)
		data(0)._1 should be (studies(0))
		data(1)._1 should be (studies(0))
		data(2)._1 should be (studies(0))
		data(3)._1 should be (studies(1))
		data(4)._1 should be (studies(1))
		data(5)._1 should be (studies(1))
		data(6)._1 should be (studies(2))
		data(7)._1 should be (studies(2))
		data(0)._2.treatment should be (ta)
		data(1)._2.treatment should be (tb)
		data(2)._2.treatment should be (tc)
		data(3)._2.treatment should be (tb)
		data(4)._2.treatment should be (tc)
		data(5)._2.treatment should be (td)
		data(6)._2.treatment should be (ta)
		data(7)._2.treatment should be (tc)
	}

	@Test def testParameterVector() {
		val model = NetworkModel(network, spanningTree)
		model.parameterVector should be (List[NetworkModelParameter](
				new BasicParameter(ta, tb),
				new BasicParameter(ta, tc),
				new BasicParameter(tb, td),
				new InconsistencyParameter(List(ta, tb, tc, ta)),
				new InconsistencyParameter(List(ta, tc, td, tb, ta))
			))
	}

	@Test def testParameterVectorOnlyInconsistencies() {
		val model = NetworkModel(network, new Tree[Treatment](
			Set((ta, tc), (tc, tb), (tb, td)), ta))
		model.parameterVector should be (List[NetworkModelParameter](
				new BasicParameter(ta, tc),
				new BasicParameter(tb, td),
				new BasicParameter(tc, tb),
				new InconsistencyParameter(List(ta, tb, tc, ta))
			))
	}

	@Test def testParameterizationBasic() {
		val model = NetworkModel(network, spanningTree)
		model.parameterization(ta, tb) should be (
			Map((new BasicParameter(ta, tb), 1)))
		model.parameterization(tb, ta) should be (
			Map((new BasicParameter(ta, tb), -1)))
	}

	@Test def testParameterizationFunctional() {
		val model = NetworkModel(network, spanningTree)
		model.parameterization(tc, td) should be (Map(
			(new BasicParameter(ta, tb), 1),
			(new BasicParameter(tb, td), 1),
			(new BasicParameter(ta, tc), -1),
			(new InconsistencyParameter(List(ta, tc, td, tb, ta)), 1)
		))
	}

	@Test def testParameterizationFunctionalWithoutIncons() {
		fail()
	}
}


class BasicParameterTest extends ShouldMatchersForJUnit {
	@Test def testToString() {
		new BasicParameter(new Treatment("A"), new Treatment("B")
			).toString should be ("d_A.B")
	}

	@Test def testEquals() {
		new BasicParameter(new Treatment("A"), new Treatment("B")) should be (
			new BasicParameter(new Treatment("A"), new Treatment("B")))
	}
}

class InconsistencyParameterTest extends ShouldMatchersForJUnit {
	@Test def testToString() {
		new InconsistencyParameter(
				List(new Treatment("A"), new Treatment("B"),
				new Treatment("C"), new Treatment("A"))
			).toString should be ("w_A.B.C")
	}

	@Test def testEquals() {
		new InconsistencyParameter(
				List(new Treatment("A"), new Treatment("B"),
				new Treatment("C"), new Treatment("A"))) should be (
			new InconsistencyParameter(
				List(new Treatment("A"), new Treatment("B"),
				new Treatment("C"), new Treatment("A")))
			)
	}
}
