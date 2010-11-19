package org.drugis.mtc.convergence;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.apache.commons.math.stat.correlation.Covariance;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.drugis.mtc.util.FileResults;
import org.drugis.mtc.util.WindowResults;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.drugis.mtc.*;
import org.drugis.mtc.yadas.RandomEffectsVariance;
import org.drugis.mtc.summary.SummaryUtil;;

/**
 * Test for assessment of convergence based on gelman.diag(X) in the R package CODA.
 * R code is in the "conv.R" resource file.
 */
public class GelmanRubinAcceptanceTest {
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
				new BasicParameter(t1, t2), new BasicParameter(t2, t3), new RandomEffectsVariance()	
		};
		d_results = new FileResults(is, d_parameters, 3, 10000);
		d_results.makeSamplesAvailable();
	}
	
	@Test
	public void testReadFiles() throws IOException {
		// currently only checks whether the samples can be read. can be removed once the other tests are un-ignored.
		assertNotNull(readExpected("conv-0.5k.txt"));
		assertNotNull(readExpected("conv-2k.txt"));
		assertNotNull(readExpected("conv-10k.txt"));
	}
	
	@Test @Ignore
	public void testResults500() throws IOException {
		double[][] expected = readExpected("conv-0.5k.txt");
		assertEquals(expected[0][0], GelmanRubinConvergence.diagnose(d_results, d_parameters[0], 1000), EPSILON);
		assertEquals(expected[1][0], GelmanRubinConvergence.diagnose(d_results, d_parameters[1], 1000), EPSILON);
		assertEquals(expected[2][0], GelmanRubinConvergence.diagnose(d_results, d_parameters[2], 1000), EPSILON);
	}
	
	@Test @Ignore
	public void testResults2k() throws IOException {
		double[][] expected = readExpected("conv-2k.txt");
		assertEquals(expected[0][0], GelmanRubinConvergence.diagnose(d_results, d_parameters[0], 4000), EPSILON);
		assertEquals(expected[1][0], GelmanRubinConvergence.diagnose(d_results, d_parameters[1], 4000), EPSILON);
		assertEquals(expected[2][0], GelmanRubinConvergence.diagnose(d_results, d_parameters[2], 4000), EPSILON);
	}
	
	@Test @Ignore
	public void testResults10k() throws IOException {
		double[][] expected = readExpected("conv-10k.txt"); // based on all samples
		assertEquals(expected[0][0], GelmanRubinConvergence.diagnose(d_results, d_parameters[0]), EPSILON);
		assertEquals(expected[1][0], GelmanRubinConvergence.diagnose(d_results, d_parameters[1]), EPSILON);
		assertEquals(expected[2][0], GelmanRubinConvergence.diagnose(d_results, d_parameters[2]), EPSILON);
	}

	private double[][] readExpected(String file) throws IOException {
		InputStream is = GelmanRubinAcceptanceTest.class.getResourceAsStream(file);
		double[][] data = new double[d_parameters.length][2];		
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		reader.readLine(); // skip the first line (column names).
		for (int i = 0; reader.ready(); ++i) {
			String line = reader.readLine();
			StringTokenizer tok = new StringTokenizer(line, ",");
			tok.nextToken(); // skip the first column (IDs)
			for (int j = 0; tok.hasMoreTokens(); ++j) {
				data[i][j] = Double.parseDouble(tok.nextToken());
			}
		}
		return data;
	}
	
	@Test
	public void testOneChainMean() {
		GelmanRubinConvergence grc = new GelmanRubinConvergence(d_results, d_parameters[0]);
		double[] samples = SummaryUtil.getOneChainLastHalfSamples(d_results, d_parameters[0], 0);
		assertEquals(d_mean.evaluate(samples), grc.oneChainMean(0), EPSILON);
		assertEquals(0.4675014, grc.oneChainMean(0), EPSILON);
	}
	
	@Test
	public void testOneChainVariance() {
		GelmanRubinConvergence grc = new GelmanRubinConvergence(d_results, d_parameters[0]);
		double[] samples = SummaryUtil.getOneChainLastHalfSamples(d_results, d_parameters[0], 0);
		assertEquals(d_var.evaluate(samples), grc.oneChainVar(0), EPSILON);
		assertEquals( 0.02467504, grc.oneChainVar(0), EPSILON);
	}
	
	@Test
	public void testAllChainsMean() {
		GelmanRubinConvergence grc = new GelmanRubinConvergence(d_results, d_parameters[0]);
		double[] samples = SummaryUtil.getAllChainsLastHalfSamples(d_results, d_parameters[0]);
		assertEquals(d_mean.evaluate(samples), grc.allChainMean(), EPSILON);
		assertEquals(0.4711288, grc.allChainMean(), EPSILON);
	}
	
	@Test
	public void testVarBetweenChains() {
		GelmanRubinConvergence grc = new GelmanRubinConvergence(d_results, d_parameters[0]);
		double[] chainMeans = new double[d_results.getNumberOfChains()];
		for(int i=0; i < d_results.getNumberOfChains(); ++i) {
			chainMeans[i] = d_mean.evaluate(SummaryUtil.getOneChainLastHalfSamples(d_results, d_parameters[0], i));
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
			chainMeans[i] = d_mean.evaluate(SummaryUtil.getOneChainLastHalfSamples(wr, d_parameters[0], i));
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
			chainMeans[i] = d_mean.evaluate(SummaryUtil.getOneChainLastHalfSamples(d_results, d_parameters[0], i));
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