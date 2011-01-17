/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
	re[2, 1] ~ dnorm(d.A.B, tau.d)
	delta[2, 1, 1] <- 0
	delta[2, 1, 2] <- re[2, 1]
	# Random effects in study 03
	re[3, 1] ~ dnorm(d.A.B + d.B.C + -w.A.B.C, tau.d)
	delta[3, 1, 1] <- 0
	delta[3, 1, 3] <- re[3, 1]

	# For each (study, treatment), model effect
	for (i in 1:length(s)) {
		logit(p[s[i], t[i]]) <- mu[s[i]] + delta[s[i], b[s[i]], t[i]]
		r[i] ~ dbin(p[s[i], t[i]], n[i])
	}

	# Meta-parameters
	d.A.B ~ dnorm(0, .001)
	d.B.C ~ dnorm(0, .001)
	w.A.B.C ~ dnorm(0, tau.w)

	# Inconsistency variance
	sd.w ~ dunif(0.00001, 1.0024677518941976)
	var.w <- sd.w * sd.w
	tau.w <- 1 / var.w

	# Random effect variance
	sd.d ~ dunif(0.00001, 1.0024677518941976)
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
			|compile, nchains(3)
			|parameters in 'jags.param1', chain(1)
			|parameters in 'jags.param2', chain(2)
			|parameters in 'jags.param3', chain(3)
			|initialize
			|
			|adapt 30000
			|
			|monitor d.A.B
			|monitor d.B.C
			|monitor w.A.B.C
			|monitor var.d
			|monitor var.w
			|
			|update 20000
			|
			|coda *, stem('jags')""".stripMargin

	val analysisText =
		"""	|deriv <- list(
			|	`d.A.C` = function(x) { x[, "d.A.B"] + x[, "d.B.C"] + -x[, "w.A.B.C"] }
			|	)
			|# source('mtc.R')
			|# data <- append.derived(read.mtc('jags'), deriv)""".stripMargin

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

	def model = new JagsSyntaxModel(
		InconsistencyNetworkModel(network, spanningTree))

	@Test def testDataText() {
		model.dataText should be (dataText)
	}

	@Test def testModelText() {
		model.modelText should be (modelText)
	}

	@Test def testScriptText() {
		model.scriptText("jags", 3, 30000, 20000) should be (scriptText)
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
	re[2, 1] ~ dnorm(d.A.B, tau.d)
	delta[2, 1, 1] <- 0
	delta[2, 1, 2] <- re[2, 1]
	# Random effects in study 03
	re[3, 1] ~ dnorm(d.A.B + d.B.C, tau.d)
	delta[3, 1, 1] <- 0
	delta[3, 1, 3] <- re[3, 1]

	# For each (study, treatment), model effect
	for (i in 1:length(s)) {
		logit(p[s[i], t[i]]) <- mu[s[i]] + delta[s[i], b[s[i]], t[i]]
		r[i] ~ dbin(p[s[i], t[i]], n[i])
	}

	# Meta-parameters
	d.A.B ~ dnorm(0, .001)
	d.B.C ~ dnorm(0, .001)

	# Random effect variance
	sd.d ~ dunif(0.00001, 1.0024677518941976)
	var.d <- sd.d * sd.d
	tau.d <- 1 / var.d

	# 2x2 inv. covariance matrix for 3-arm trials
	var.2[1, 1] <- var.d
	var.2[1, 2] <- var.d / 2
	var.2[2, 1] <- var.d / 2
	var.2[2, 2] <- var.d
	tau.2 <- inverse(var.2)
}"""

	val initText =
		""" |`d.A.B` <-
			|0.0
			|`d.B.C` <-
			|0.0
			|`mu` <-
			|c(0.0,0.0,0.0)
			|`re` <-
			|structure(c(0.0,0.0,0.0,0.0,NA,NA), .Dim = c(3L,2L))
			|`sd.d` <-
			|0.5012338759470988""".stripMargin

	val scriptText =
		"""	|model in 'jags.model'
			|data in 'jags.data'
			|compile, nchains(3)
			|parameters in 'jags.param1', chain(1)
			|parameters in 'jags.param2', chain(2)
			|parameters in 'jags.param3', chain(3)
			|initialize
			|
			|adapt 30000
			|
			|monitor d.A.B
			|monitor d.B.C
			|monitor var.d
			|
			|update 20000
			|
			|coda *, stem('jags')""".stripMargin

	val analysisText =
		"""	|deriv <- list(
			|	`d.A.C` = function(x) { x[, "d.A.B"] + x[, "d.B.C"] }
			|	)
			|# source('mtc.R')
			|# data <- append.derived(read.mtc('jags'), deriv)""".stripMargin

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

	val baselines = Map[Study[DichotomousMeasurement], Treatment](
		(network.study("01"), tb),
		(network.study("02"), ta),
		(network.study("03"), ta)
	)

	val proto = ConsistencyNetworkModel(network, spanningTree, baselines)

	def model = new JagsSyntaxModel(proto)

	@Test def testDataText() {
		model.dataText should be (dataText)
	}

	@Test def testModelText() {
		model.modelText should be (modelText)
	}

	@Test def testInitText() {
		model.initialValuesText(new PriorStartingValueGenerator(proto)) should be (initText)
	}

	@Test def testScriptText() {
		model.scriptText("jags", 3, 30000, 20000) should be (scriptText)
	}

	@Test def testAnalysisText() {
		model.analysisText("jags") should be (analysisText)
	}
}

class JagsSyntaxContinuousModelTest extends ShouldMatchersForJUnit {
	val dataText = """s <- c(1, 1, 1, 2, 2, 3, 3)
t <- c(1, 2, 3, 1, 2, 1, 3)
m <- c(10.0, 8.0, 9.0, 13.0, 11.0, 13.0, 11.0)
e <- c(1.0, 2.0, 1.5, 1.0, 1.0, 1.0, 1.0)
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
	re[2, 1] ~ dnorm(d.A.B, tau.d)
	delta[2, 1, 1] <- 0
	delta[2, 1, 2] <- re[2, 1]
	# Random effects in study 03
	re[3, 1] ~ dnorm(d.A.B + d.B.C, tau.d)
	delta[3, 1, 1] <- 0
	delta[3, 1, 3] <- re[3, 1]

	# For each (study, treatment), model effect
	for (i in 1:length(s)) {
		p[s[i], t[i]] <- mu[s[i]] + delta[s[i], b[s[i]], t[i]]
		m[i] ~ dnorm(p[s[i], t[i]], 1 / (e[i] * e[i]))
	}

	# Meta-parameters
	d.A.B ~ dnorm(0, .001)
	d.B.C ~ dnorm(0, .001)

	# Random effect variance
	sd.d ~ dunif(0.00001, 1.0)
	var.d <- sd.d * sd.d
	tau.d <- 1 / var.d

	# 2x2 inv. covariance matrix for 3-arm trials
	var.2[1, 1] <- var.d
	var.2[1, 2] <- var.d / 2
	var.2[2, 1] <- var.d / 2
	var.2[2, 2] <- var.d
	tau.2 <- inverse(var.2)
}"""

	val inconsModelText = """model {
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
	re[2, 1] ~ dnorm(d.A.B, tau.d)
	delta[2, 1, 1] <- 0
	delta[2, 1, 2] <- re[2, 1]
	# Random effects in study 03
	re[3, 1] ~ dnorm(d.A.B + d.B.C + -w.A.B.C, tau.d)
	delta[3, 1, 1] <- 0
	delta[3, 1, 3] <- re[3, 1]

	# For each (study, treatment), model effect
	for (i in 1:length(s)) {
		p[s[i], t[i]] <- mu[s[i]] + delta[s[i], b[s[i]], t[i]]
		m[i] ~ dnorm(p[s[i], t[i]], 1 / (e[i] * e[i]))
	}

	# Meta-parameters
	d.A.B ~ dnorm(0, .001)
	d.B.C ~ dnorm(0, .001)
	w.A.B.C ~ dnorm(0, tau.w)

	# Inconsistency variance
	sd.w ~ dunif(0.00001, 1.0)
	var.w <- sd.w * sd.w
	tau.w <- 1 / var.w

	# Random effect variance
	sd.d ~ dunif(0.00001, 1.0)
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
			|compile, nchains(3)
			|parameters in 'jags.param1', chain(1)
			|parameters in 'jags.param2', chain(2)
			|parameters in 'jags.param3', chain(3)
			|initialize
			|
			|adapt 30000
			|
			|monitor d.A.B
			|monitor d.B.C
			|monitor var.d
			|
			|update 20000
			|
			|coda *, stem('jags')""".stripMargin

	val analysisText =
		"""	|deriv <- list(
			|	`d.A.C` = function(x) { x[, "d.A.B"] + x[, "d.B.C"] }
			|	)
			|# source('mtc.R')
			|# data <- append.derived(read.mtc('jags'), deriv)""".stripMargin

	def network = Network.contFromXML(
		<network type="continuous">
			<treatments>
				<treatment id="A"/>
				<treatment id="B"/>
				<treatment id="C"/>
			</treatments>
			<studies>
				<study id="01">
					<measurement treatment="A" mean="10" standardDeviation="4" sample="16" />
					<measurement treatment="B" mean="8" standardDeviation="8" sample="16" />
					<measurement treatment="C" mean="9" standardDeviation="6" sample="16" />
				</study>
				<study id="02">
					<measurement treatment="A" mean="13" standardDeviation="8" sample="64" />
					<measurement treatment="B" mean="11" standardDeviation="8" sample="64" />
				</study>
				<study id="03">
					<measurement treatment="A" mean="13" standardDeviation="8" sample="64" />
					<measurement treatment="C" mean="11" standardDeviation="8" sample="64" />
				</study>
			</studies>
		</network>)

	val ta = new Treatment("A")
	val tb = new Treatment("B")
	val tc = new Treatment("C")

	val spanningTree = new Tree[Treatment](
		Set((ta, tb), (tb, tc)), ta)

	val baselines = Map[Study[ContinuousMeasurement], Treatment](
		(network.study("01"), tb),
		(network.study("02"), ta),
		(network.study("03"), ta)
	)

	def model = new JagsSyntaxModel(
		ConsistencyNetworkModel(network, spanningTree, baselines))

	def inconsModel = new JagsSyntaxModel(
		InconsistencyNetworkModel(network, spanningTree))

	@Test def testDataText() {
		model.dataText should be (dataText)
	}

	@Test def testModelText() {
		model.modelText should be (modelText)
	}

	@Test def testScriptText() {
		model.scriptText("jags", 3, 30000, 20000) should be (scriptText)
	}

	@Test def testAnalysisText() {
		model.analysisText("jags") should be (analysisText)
	}

	@Test def testInconsistencyModelText() {
		inconsModel.modelText should be (inconsModelText)
	}
}

class JagsSyntaxNodeSplitModelTest extends ShouldMatchersForJUnit {
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
	re[1, 1] ~ dnorm(-d.A.B.dir, tau.d)
	re[1, 2] ~ dnorm(d.B.C, tau.d)
	delta[1, 2, 2] <- 0
	delta[1, 2, 1] <- re[1, 1]
	delta[1, 2, 3] <- re[1, 2]
	# Random effects in study 02
	re[2, 1] ~ dnorm(d.A.B.dir, tau.d)
	delta[2, 1, 1] <- 0
	delta[2, 1, 2] <- re[2, 1]
	# Random effects in study 03
	re[3, 1] ~ dnorm(d.A.B.ind + d.B.C, tau.d)
	delta[3, 1, 1] <- 0
	delta[3, 1, 3] <- re[3, 1]

	# For each (study, treatment), model effect
	for (i in 1:length(s)) {
		logit(p[s[i], t[i]]) <- mu[s[i]] + delta[s[i], b[s[i]], t[i]]
		r[i] ~ dbin(p[s[i], t[i]], n[i])
	}

	# Meta-parameters
	d.A.B.dir ~ dnorm(0, .001)
	d.A.B.ind ~ dnorm(0, .001)
	d.B.C ~ dnorm(0, .001)

	# Random effect variance
	sd.d ~ dunif(0.00001, 1.0024677518941976)
	var.d <- sd.d * sd.d
	tau.d <- 1 / var.d
}"""

	val initText =
		""" |`d.A.B.dir` <-
			|0.0
			|`d.A.B.ind` <-
			|0.0
			|`d.B.C` <-
			|0.0
			|`mu` <-
			|c(0.0,0.0,0.0)
			|`re` <-
			|structure(c(0.0,0.0,0.0,0.0,NA,NA), .Dim = c(3L,2L))
			|`sd.d` <-
			|0.5012338759470988""".stripMargin

	val scriptText =
		"""	|model in 'jags.model'
			|data in 'jags.data'
			|compile, nchains(3)
			|parameters in 'jags.param1', chain(1)
			|parameters in 'jags.param2', chain(2)
			|parameters in 'jags.param3', chain(3)
			|initialize
			|
			|adapt 30000
			|
			|monitor d.A.B.dir
			|monitor d.A.B.ind
			|monitor d.B.C
			|monitor var.d
			|
			|update 20000
			|
			|coda *, stem('jags')""".stripMargin

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

	val baselines = Map[Study[DichotomousMeasurement], Treatment](
		(network.study("01"), tb),
		(network.study("02"), ta),
		(network.study("03"), ta)
	)

	val proto = NodeSplitNetworkModel(network, (ta, tb), spanningTree, baselines)

	def model = new JagsSyntaxModel(proto)

	@Test def testDataText() {
		model.dataText should be (dataText)
	}

	@Test def testModelText() {
		model.modelText should be (modelText)
	}

	@Test def testInitText() {
		model.initialValuesText(new PriorStartingValueGenerator(proto)) should be (initText)
	}

	@Test def testScriptText() {
		model.scriptText("jags", 3, 30000, 20000) should be (scriptText)
	}
}
