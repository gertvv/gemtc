package org.drugis.mtc.jags

import org.drugis.mtc._

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

class JagsJniInconsistencyModelTest extends ShouldMatchersForJUnit {
	def network = Network.fromXML(
		<network description="Smoking cessation rates">
			<treatments>
				<treatment id="A">No Contact</treatment>
				<treatment id="B">Self-help</treatment>
				<treatment id="C">Individual Counseling</treatment>
			</treatments>
			<studies>
				<study id="01">
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

	def model = new JagsJniInconsistencyModel(
		NetworkModel(network, spanningTree))

	@Before def setUp() {
		JagsModelFactory.loadModules()
	}

	@Test def testIsNotReady() {
		model.isReady should be (false)
	}

	@Test def testRun() {
		val m = model
		m.run()
		m.isReady should be (true)
		val dAB = m.getRelativeEffect(ta, tb)
		val dBC = m.getRelativeEffect(tb, tc)
		val wACB = m.getInconsistency(
			new InconsistencyParameter(List(ta, tc, tb, ta)))

		// Values below obtained via a run through regular JAGS with 30k/20k
		// iterations. Taking .15 sd as acceptable margin (same as JAGS does
		// for testing against WinBUGS results).
		val f = 0.15
		val mAB = 0.2555623
		val sAB = 0.9644176
		dAB.getMean should be (mAB plusOrMinus f * sAB)
		dAB.getStandardDeviation should be(sAB plusOrMinus f * sAB)
		val mBC = -0.6063327
		val sBC = 0.9987635
		dBC.getMean should be (mBC plusOrMinus f * sBC)
		dBC.getStandardDeviation should be(sBC plusOrMinus f * sBC)
		val mACB = 0.4334485
		val sACB = 0.915094
		wACB.getMean should be (mACB plusOrMinus f * sACB)
		wACB.getStandardDeviation should be(sACB plusOrMinus f * sACB)
	}

	@Test def testInconsistencyFactors() {
		val list = model.getInconsistencyFactors
		list.size should be (1)
		list.contains(
			new InconsistencyParameter(List(ta, tc, tb, ta))) should be (true)
	}
}
