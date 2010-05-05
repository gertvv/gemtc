package org.drugis.mtc.yadas

import gov.lanl.yadas.Likelihood
import org.apache.commons.math.linear.Array2DRowRealMatrix

/**
 * Computes the log of the probability density for a multivariate Gaussian.
 * The double-arrays of sigma are the rows of the covariance matrix.
 */
class MultivariateGaussian extends Likelihood {
	override def compute(args: Array[Array[Double]]): Double = {
		compute(args(0), args(1), args.subArray(2, args.length))
	}

	def compute(x: Array[Double],
			mu: Array[Double],
			sigma: Array[Array[Double]])
	: Double = {
		val d = x.length
		if (d != mu.length || d != sigma.length)
			throw new IllegalArgumentException(
				"All arguments need to be of equal length")

		val sigmaM = new Array2DRowRealMatrix(sigma)
		val xM = new Array2DRowRealMatrix(x)
		val muM = new Array2DRowRealMatrix(mu)

		// Return the log of:
		// 1/sqrt(2pi^d * det(sigma)) * e^(-.5 (x - mu)' inv(sigma) (x - mu))
		// Which is:
		// -log(sqrt(2pi^d * det(sigma))) + -.5 (x - mu)' inv(sigma) (x - mu) 
		val dM = xM.subtract(muM)
		-0.5 * (
			Math.log(2 * Math.Pi) * d + Math.log(sigmaM.getDeterminant) +
			dM.transpose.multiply(sigmaM.inverse).multiply(dM).getEntry(0, 0))
	}
}
