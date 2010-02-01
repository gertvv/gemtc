package org.drugis.mtc

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
		val model = NetworkModel(network, new Tree[Treatment](
			Set((ta, tc), (tc, tb), (tb, td)), ta))
		model.parameterization(tc, td) should be (Map(
			(new BasicParameter(tc, tb), 1),
			(new BasicParameter(tb, td), 1)
		))
	}
}


class BasicParameterTest extends ShouldMatchersForJUnit {
	@Test def testToString() {
		new BasicParameter(new Treatment("A"), new Treatment("B")
			).toString should be ("d.A.B")
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
			).toString should be ("w.A.B.C")
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

class JagsSyntaxInconsistencyModelTest extends ShouldMatchersForJUnit {
	val dataText = """s <- c(1, 1, 1, 2, 2, 3, 3)
t <- c(1, 2, 3, 1, 2, 1, 3)
r <- c(9, 23, 10, 79, 77, 18, 21)
n <- c(140, 140, 138, 702, 694, 671, 535)
b <- c(2, 1, 1)"""

	val modelText = """model {
	# Study baseline effects
	for (i in 1:length(b)) {
		mu[i] ~ dnorm(0, .001)
	}

	# Random effects in study 01
	d[1, 1] <- -d.A.B
	d[1, 2] <- d.B.C
	re[1, 1:2] ~ dmnorm(d[1, 1:2], tau.2)
	delta[1, 2, 2] <- 0
	delta[1, 2, 1] <- re[1, 1]
	delta[1, 2, 3] <- re[1, 2]
	# Random effects in study 02
	delta[2, 1, 1] <- 0
	delta[2, 1, 2] ~ dnorm(d.A.B, tau.d)
	# Random effects in study 03
	delta[3, 1, 1] <- 0
	delta[3, 1, 3] ~ dnorm(d.A.B + d.B.C + w.A.C.B, tau.d)

	# For each (study, treatment), model effect
	for (i in 1:length(s)) {
		logit(p[s[i], t[i]]) <- mu[s[i]] + delta[s[i], b[s[i]], t[i]]
		r[i] ~ dbin(p[s[i], t[i]], n[i])
	}

	# Basic parameters and inconsistencies
	d.A.B ~ dnorm(0, .001)
	d.B.C ~ dnorm(0, .001)
	w.A.C.B ~ dnorm(0, tau.w)

	# Inconsistency variance
	sd.w ~ dunif(0.00001, 2)
	var.w <- sd.w * sd.w
	tau.w <- 1 / var.w

	# Random effect variance
	sd.d ~ dunif(0.00001, 2)
	var.d <- sd.d * sd.d
	tau.d <- 1 / var.d

	# 2x2 inv. covariance matrix for 3-arm trials
	var.2[1, 1] <- var.d
	var.2[1, 2] <- var.d / 2
	var.2[2, 1] <- var.d / 2
	var.2[2, 2] <- var.d
	tau.2 <- inverse(var.2)
}"""

	val scriptText =
		"""	|model in 'jags.model'
			|data in 'jags.data'
			|compile
			|initialize
			|
			|update 30000
			|
			|monitor d.A.B
			|monitor d.B.C
			|monitor w.A.C.B
			|monitor var.d
			|monitor var.w
			|
			|update 20000
			|
			|monitors to 'jags.R'""".stripMargin

	val analysisText =
		"""	|source('jags.R')
			|attach(trace)
			|data <- list()
			|data$d.A.B <- d.A.B
			|data$d.A.C <- d.A.B + d.B.C + w.A.C.B
			|data$d.B.C <- d.B.C
			|data$w.A.C.B <- w.A.C.B
			|data$var.d <- var.d
			|data$var.w <- var.w
			|detach(trace)""".stripMargin

	def network = Network.fromXML(
		<network description="Smoking cessation rates">
			<treatments>
				<treatment id="A">No Contact</treatment>
				<treatment id="B">Self-help</treatment>
				<treatment id="C">Individual Counseling</treatment>
			</treatments>
			<studies>
				<study id="01">
					<measurement treatment="A" responders="9" sample="140" />
					<measurement treatment="B" responders="23" sample="140" />
					<measurement treatment="C" responders="10" sample="138" />
				</study>
				<study id="02">
					<measurement treatment="A" responders="79" sample="702" />
					<measurement treatment="B" responders="77" sample="694" />
				</study>
				<study id="03">
					<measurement treatment="A" responders="18" sample="671" />
					<measurement treatment="C" responders="21" sample="535" />
				</study>
			</studies>
		</network>)

	val ta = new Treatment("A")
	val tb = new Treatment("B")
	val tc = new Treatment("C")

	val spanningTree = new Tree[Treatment](
		Set((ta, tb), (tb, tc)), ta)

	def model = new JagsSyntaxInconsistencyModel(
		NetworkModel(network, spanningTree))

	@Test def testDataText() {
		model.dataText should be (dataText)
	}

	@Test def testModelText() {
		model.modelText should be (modelText)
	}

	@Test def testScriptText() {
		model.scriptText("jags") should be (scriptText)
	}

	@Test def testAnalysisText() {
		model.analysisText("jags") should be (analysisText)
	}
}
