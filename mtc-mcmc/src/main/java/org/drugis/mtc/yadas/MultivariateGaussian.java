/*
 * This file is part of the GeMTC software for MTC model generation and
 * analysis. GeMTC is distributed from http://drugis.org/gemtc.
 * Copyright (C) 2009-2012 Gert van Valkenhoef.
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

package org.drugis.mtc.yadas;

import gov.lanl.yadas.Likelihood;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.Precision;

/**
 * Computes the log of the probability density for a multivariate Gaussian.
 * The double-arrays of sigma are the rows of the covariance matrix.
 */
public class MultivariateGaussian implements Likelihood {
	public double compute(double[][] data) {
		double[][] other = new double[data.length - 2][];
		System.arraycopy(data, 2, other, 0, data.length - 2);
		return compute(data[0], data[1], other);
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
		LUDecomposition sigmaD = new LUDecomposition(sigmaM, Precision.SAFE_MIN);
		try {
			RealMatrix sigmaInv = sigmaD.getSolver().getInverse();
			return -0.5 * (
				Math.log(2 * Math.PI) * d + Math.log(sigmaD.getDeterminant()) +
				dM.transpose().multiply(sigmaInv).multiply(dM).getEntry(0, 0));
		} catch (RuntimeException e) {
			System.out.println(sigmaM);
			throw e;
		}
/*		RealMatrix sigmaInv = sigmaM.inverse();
		return -0.5 * (
			Math.log(2 * Math.PI) * d + Math.log(sigmaM.getDeterminant()) +
			dM.transpose().multiply(sigmaInv).multiply(dM).getEntry(0, 0)); */
	}

}
