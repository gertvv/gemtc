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

package org.drugis.mtc.convergence;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.parameterization.RandomEffectsStandardDeviation;
import org.drugis.mtc.summary.SummaryUtil;
import org.drugis.mtc.test.FileResults;
import org.drugis.mtc.util.WindowResults;
import org.junit.Before;
import org.junit.Test;

public class GelmanRubinTest {
	private static final double EPSILON = 0.0000001;
	private Parameter[] d_parameters;
	private FileResults d_results;
	private Mean d_mean = new Mean();
    private Variance d_var = new Variance();
    
	@Before
	public void setUp() throws IOException {
		InputStream is = GelmanRubinAcceptanceTest.class.getResourceAsStream("conv-samples.txt");
		Treatment t1 = new Treatment("iPCI");
		Treatment t2 = new Treatment("mPCI");
		Treatment t3 = new Treatment("sPCI");
		d_parameters = new Parameter[] {
				new BasicParameter(t1, t2), new BasicParameter(t2, t3), new RandomEffectsStandardDeviation()	
		};
		d_results = new FileResults(is, d_parameters, 3, 10000);
		d_results.makeSamplesAvailable();
	}

	@Test
	public void testOneChainMean() {
		GelmanRubinConvergence grc = new GelmanRubinConvergence(d_results, d_parameters[0]);
		List<Double> samples = SummaryUtil.getOneChainLastHalfSamples(d_results, d_parameters[0], 0);
		assertEquals(SummaryUtil.evaluate(d_mean, samples), grc.oneChainMean(0), EPSILON);
		assertEquals(0.4675014, grc.oneChainMean(0), EPSILON);
	}
	
	@Test
	public void testOneChainVariance() {
		GelmanRubinConvergence grc = new GelmanRubinConvergence(d_results, d_parameters[0]);
		List<Double> samples = SummaryUtil.getOneChainLastHalfSamples(d_results, d_parameters[0], 0);
		assertEquals(SummaryUtil.evaluate(d_var, samples), grc.oneChainVar(0), EPSILON);
		assertEquals( 0.02467504, grc.oneChainVar(0), EPSILON);
	}
	
	@Test
	public void testAllChainsMean() {
		GelmanRubinConvergence grc = new GelmanRubinConvergence(d_results, d_parameters[0]);
		List<Double> samples = SummaryUtil.getAllChainsLastHalfSamples(d_results, d_parameters[0]);
		assertEquals(SummaryUtil.evaluate(d_mean, samples), grc.allChainMean(), EPSILON);
		assertEquals(0.4711288, grc.allChainMean(), EPSILON);
	}
	
	@Test
	public void testVarBetweenChains() {
		GelmanRubinConvergence grc = new GelmanRubinConvergence(d_results, d_parameters[0]);
		double[] chainMeans = new double[d_results.getNumberOfChains()];
		for(int i=0; i < d_results.getNumberOfChains(); ++i) {
			chainMeans[i] = SummaryUtil.evaluate(d_mean, SummaryUtil.getOneChainLastHalfSamples(d_results, d_parameters[0], i));
		}
		assertEquals(d_var.evaluate(chainMeans) * d_results.getNumberOfSamples() / 2, grc.getBetweenChainVar(), EPSILON);
		assertEquals(0.05104937, grc.getBetweenChainVar(), EPSILON);
	}
	
	@Test
	public void testVarBetweenShortChains() {
		WindowResults wr = new WindowResults(d_results, 900, 930);
		GelmanRubinConvergence grc = new GelmanRubinConvergence(wr, d_parameters[0]);
		double[] chainMeans = new double[wr.getNumberOfChains()];
		for(int i=0; i < wr.getNumberOfChains(); ++i) {
			chainMeans[i] = SummaryUtil.evaluate(d_mean, SummaryUtil.getOneChainLastHalfSamples(wr, d_parameters[0], i));
		}
		assertEquals(d_var.evaluate(chainMeans) * wr.getNumberOfSamples() / 2, grc.getBetweenChainVar(), EPSILON);
	}
	
	@Test
	public void testVarWithinChains() {
		GelmanRubinConvergence grc = new GelmanRubinConvergence(d_results, d_parameters[0]);
		double var = 0;
		int m = d_results.getNumberOfChains();
		for(int i=0; i<m; ++i) {
			var += grc.oneChainVar(i);
		}
		assertEquals(var / m, grc.getWithinChainVar(), EPSILON);
		assertEquals(0.0253336, grc.getWithinChainVar(), EPSILON);
	}
	
	@Test 
	public void testSigmaSquaredHat() {
		GelmanRubinConvergence grc = new GelmanRubinConvergence(d_results, d_parameters[0]);
		int n = grc.getNSamples();
		int m = d_results.getNumberOfChains();
		double w = 0;
		double[] chainMeans = new double[d_results.getNumberOfChains()];

		for(int i=0; i<m; ++i) {
			w += grc.oneChainVar(i);
			chainMeans[i] = SummaryUtil.evaluate(d_mean, SummaryUtil.getOneChainLastHalfSamples(d_results, d_parameters[0], i));
		}
		w /= m;
		double b = d_var.evaluate(chainMeans) * d_results.getNumberOfSamples() / 2;
		double sigmahat = w * (n - 1) / n + b / n;
		assertEquals(sigmahat, grc.getSigmaSquaredHat(), EPSILON);
		assertEquals(0.02533874342262478, grc.getSigmaSquaredHat(), EPSILON);

	}
	
	@Test
	public void testVarEstimate() {
		GelmanRubinConvergence grc = new GelmanRubinConvergence(d_results, d_parameters[0]);
		assertEquals(0.02534215, grc.getVHat(), EPSILON);
	}
	
	@Test
	public void testDegreesOfFreedom() {
		GelmanRubinConvergence grc = new GelmanRubinConvergence(d_results, d_parameters[0]);
		double m = grc.getNChains();
		double n = grc.getNSamples();
		Covariance cov = new Covariance();
 
		double [] squaredMeans = grc.getMeans();
		for (int i = 0; i < grc.getNChains(); ++i) squaredMeans[i] = Math.pow(squaredMeans[i], 2); 
		
		double covWB = (n / m) * (cov.covariance(grc.getVariances(), squaredMeans) 
						- 2 * grc.allChainMean() * cov.covariance(grc.getVariances(), grc.getMeans()));
		assertEquals(-5.062888e-06, covWB, EPSILON);
		double varW = d_var.evaluate(grc.getVariances()) / m;
		double varB = 2 * grc.getBetweenChainVar() * grc.getBetweenChainVar() / (m - 1);
		assertEquals(1.091613e-07, varW, EPSILON);
		assertEquals(0.002606038, varB, EPSILON);
		
		double varV = ( Math.pow(n - 1, 2) * varW + Math.pow(1 + 1 / m, 2) 
						* varB + 2 * (n - 1) * (1 + 1 / m) * covWB) / (n * n);
		assertEquals(1.066032e-07, varV, EPSILON);
		assertEquals(2 * grc.getVHat() * grc.getVHat() / varV, grc.getDegreesOfFreedom(), EPSILON);
		assertEquals(12048.8717418775, grc.getDegreesOfFreedom(), EPSILON); 
	}
	
	@Test
	public void testCorrectedScaleReductionFactor() {
		GelmanRubinConvergence grc = new GelmanRubinConvergence(d_results, d_parameters[0]);
		assertEquals(1.000252, grc.getCorrPSRF(), EPSILON*10);
	}
}
