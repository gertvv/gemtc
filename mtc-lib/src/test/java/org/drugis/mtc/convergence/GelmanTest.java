package org.drugis.mtc.convergence;

import java.io.IOException;
import java.io.InputStream;

import org.drugis.mtc.util.FileResults;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.drugis.mtc.*;
import org.drugis.mtc.yadas.RandomEffectsVariance;

/**
 * Test for assessment of convergence based on gelman.diag(X) in the R package CODA.
 * R code is in the "conv.R" resource file.
 */
public class GelmanTest {
	@Before
	public void setUp() throws IOException {
		InputStream is = GelmanTest.class.getResourceAsStream("conv-samples.txt");
		Treatment t1 = new Treatment("iPCI");
		Treatment t2 = new Treatment("mPCI");
		Treatment t3 = new Treatment("sPCI");
		FileResults results = new FileResults(is, new Parameter[] {
				new BasicParameter(t1, t2), new BasicParameter(t2, t3), new RandomEffectsVariance()	
		}, 3, 10000);
	}
	
	@Test
	public void testReadFiles() {
		// currently only checks whether the samples can be read.
	}
	
	@Test @Ignore
	public void testResults500() {
		// FIXME: test results after 500 iterations
		// read file: conv-0.5k.txt
		// d_results.makeSamplesAvailable(500);
	}
	
	@Test @Ignore
	public void testResults2k() {
		// FIXME: test results after 2,000 iterations
		// read file: conv-2k.txt
		// d_results.makeSamplesAvailable(2000);
	}
	
	@Test @Ignore
	public void testResults10k() {
		// FIXME: test results after 10,000 iterations
		// read file: conv-10k.txt
		// d_results.makeSamplesAvailable(); // all samples
	}
}