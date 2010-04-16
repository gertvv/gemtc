package org.drugis.mtc

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

abstract class InconsistencyModelTestBase extends ShouldMatchersForJUnit {
	val f = 0.15
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

	def makeModel(nm: NetworkModel[DichotomousMeasurement]): InconsistencyModel

	var model: InconsistencyModel = null 

	@Before def setUp() {
		model = makeModel(NetworkModel(network, spanningTree))
	}

	@Test def testIsNotReady() {
		model.isReady should be (false)
	}

	@Test def testBasicParameters() {
		model.run()
		model.isReady should be (true)

		val m = model
		val dAB = m.getRelativeEffect(ta, tb)
		val dBC = m.getRelativeEffect(tb, tc)
		val wACB = m.getInconsistency(
			new InconsistencyParameter(List(ta, tc, tb, ta)))

		// Values below obtained via a run through regular JAGS with 30k/20k
		// iterations. Taking .15 sd as acceptable margin (same as JAGS does
		// for testing against WinBUGS results).
		val mAB = 0.493260093404085
		val sAB = 0.748945563653493
		dAB.getMean should be (mAB plusOrMinus f * sAB)
		dAB.getStandardDeviation should be(sAB plusOrMinus f * sAB)
		val mBC = -0.515757678945450
		val sBC = 0.938949102208760
		dBC.getMean should be (mBC plusOrMinus f * sBC)
		dBC.getStandardDeviation should be(sBC plusOrMinus f * sBC)
		val mACB = 0.215175403524793
		val sACB = 0.753639001832770
		wACB.getMean should be (mACB plusOrMinus f * sACB)
		wACB.getStandardDeviation should be(sACB plusOrMinus f * sACB)
	}

	@Test def testDerivedParameters() {
		model.run()
		model.isReady should be (true)

		val m = model

		val dBA = m.getRelativeEffect(tb, ta)
		dBA should not be (null)
		val dAC = m.getRelativeEffect(ta, tc)
		val mAC = 0.192677817983428
		val sAC = 0.853568634052751
		dAC.getMean should be (mAC plusOrMinus f * sAC)
		dAC.getStandardDeviation should be(sAC plusOrMinus f * sAC)
	}

	@Test def testInconsistencyFactors() {
		val list = model.getInconsistencyFactors
		list.size should be (1)
		list.contains(
			new InconsistencyParameter(List(ta, tc, tb, ta))) should be (true)
	}
}
