package org.drugis.mtc.yadas

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

class MultivariateGaussianTest extends ShouldMatchersForJUnit {
	val mu = Array(0.1, 0.3)
	val sigma = Array(Array(0.7, 0.1), Array(0.1, 0.5))
	val eps = 0.0001
	val mvg = new MultivariateGaussian()

	@Test def testCompute1() {
		val x = Array(0.5, 0.3)
		mvg.compute(x, mu, sigma) should be (-1.416119 plusOrMinus eps)
	}

	@Test def testCompute2() {
		val x = Array(0.3, 0.5)
		mvg.compute(x, mu, sigma) should be (-1.357296 plusOrMinus eps)
	}

	@Test def testCompute3() {
		val x = Array(0.1, 0.3)
		mvg.compute(x, mu, sigma) should be (-1.298472 plusOrMinus eps)
	}

	@Test def testCompute4() {
		val x = Array(10.0, 0)
		mvg.compute(x, mu, sigma) should be (-74.33083 plusOrMinus eps)
	}

	@Test def testCompute5() {
		val x = Array(-3.0, 2)
		mvg.compute(x, mu, sigma) should be (-12.88965 plusOrMinus eps)
	}
}
