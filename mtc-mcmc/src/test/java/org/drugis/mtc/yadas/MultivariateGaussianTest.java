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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class MultivariateGaussianTest {
	private static final double EPSILON = 0.000005;
	private double[] d_mu;
	private double[][] d_sigma;
	private MultivariateGaussian d_mvg;

	@Before
	public void setUp() {
		d_mu = new double[] {0.1, 0.3};
		d_sigma = new double[][] {{0.7, 0.1}, {0.1, 0.5}};
		d_mvg = new MultivariateGaussian();
	}
	
	@Test
	public void testComputeInternal() {
		assertEquals(-1.416119, d_mvg.compute(new double[] {0.5, 0.3}, d_mu, d_sigma), EPSILON);
		assertEquals(-1.357296, d_mvg.compute(new double[] {0.3, 0.5}, d_mu, d_sigma), EPSILON);
		assertEquals(-1.298472, d_mvg.compute(new double[] {0.1, 0.3}, d_mu, d_sigma), EPSILON);
		assertEquals(-74.33083, d_mvg.compute(new double[] {10.0, 0.0}, d_mu, d_sigma), EPSILON * 10);
		assertEquals(-12.88965, d_mvg.compute(new double[] {-3.0, 2.0}, d_mu, d_sigma), EPSILON * 10);
	}
	
	@Test
	public void testCompute() {
		double[][] input = new double[][] { {0.5, 0.3}, d_mu, d_sigma[0], d_sigma[1] };
		assertEquals(-1.416119, d_mvg.compute(input), EPSILON);
	}
}