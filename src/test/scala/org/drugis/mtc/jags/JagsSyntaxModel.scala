package org.drugis.mtc.jags

import org.drugis.mtc._

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

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

	# Basic parameters
	d.A.B ~ dnorm(0, .001)
	d.B.C ~ dnorm(0, .001)

	# Inconsistency factors
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

	def network = Network.dichFromXML(
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

class JagsSyntaxConsistencyModelTest extends ShouldMatchersForJUnit {
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
	delta[3, 1, 3] ~ dnorm(d.A.B + d.B.C, tau.d)

	# For each (study, treatment), model effect
	for (i in 1:length(s)) {
		logit(p[s[i], t[i]]) <- mu[s[i]] + delta[s[i], b[s[i]], t[i]]
		r[i] ~ dbin(p[s[i], t[i]], n[i])
	}

	# Basic parameters
	d.A.B ~ dnorm(0, .001)
	d.B.C ~ dnorm(0, .001)

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
			|monitor var.d
			|
			|update 20000
			|
			|monitors to 'jags.R'""".stripMargin

	val analysisText =
		"""	|source('jags.R')
			|attach(trace)
			|data <- list()
			|data$d.A.B <- d.A.B
			|data$d.A.C <- d.A.B + d.B.C
			|data$d.B.C <- d.B.C
			|data$var.d <- var.d
			|detach(trace)""".stripMargin

	def network = Network.dichFromXML(
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

	def model = new JagsSyntaxConsistencyModel(
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
