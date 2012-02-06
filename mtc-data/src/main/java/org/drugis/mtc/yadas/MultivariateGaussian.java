package org.drugis.mtc.yadas;

import java.util.Arrays;

import gov.lanl.yadas.Likelihood;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.LUDecomposition;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;

/**
 * Computes the log of the probability density for a multivariate Gaussian.
 * The double-arrays of sigma are the rows of the covariance matrix.
 */
public class MultivariateGaussian implements Likelihood {
	@Override
	public double compute(double[][] data) {
		return compute(data[0], data[1], Arrays.copyOfRange(data, 2, data.length));
	}

	public double compute(double[] x, double[] mu, double[][] sigma) {
		int d = x.length;
		if (d != mu.length || d != sigma.length) {
			throw new IllegalArgumentException("All arguments need to be of equal length");
		}

		Array2DRowRealMatrix sigmaM = new Array2DRowRealMatrix(sigma);
		Array2DRowRealMatrix xM = new Array2DRowRealMatrix(x);
		Array2DRowRealMatrix muM = new Array2DRowRealMatrix(mu);

		// Return the log of:
		// 1/sqrt(2pi^d * det(sigma)) * e^(-.5 (x - mu)' inv(sigma) (x - mu))
		// Which is:
		// -log(sqrt(2pi^d * det(sigma))) + -.5 (x - mu)' inv(sigma) (x - mu) 
		Array2DRowRealMatrix dM = xM.subtract(muM);
		LUDecomposition sigmaD = new LUDecompositionImpl(sigmaM);
		RealMatrix sigmaInv = sigmaD.getSolver().getInverse();
		return -0.5 * (
			Math.log(2 * Math.PI) * d + Math.log(sigmaD.getDeterminant()) +
			dM.transpose().multiply(sigmaInv).multiply(dM).getEntry(0, 0));
	}
}
