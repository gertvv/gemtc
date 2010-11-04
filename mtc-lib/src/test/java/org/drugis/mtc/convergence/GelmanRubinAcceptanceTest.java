package org.drugis.mtc.convergence;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

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
public class GelmanRubinAcceptanceTest {
	private static final double EPSILON = 0.0000001;
	private Parameter[] d_parameters;
	private FileResults d_results;

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
		assertEquals(expected[0][0], GelmanRubinConvergence.diagnose(d_results, d_parameters[0], 500), EPSILON);
		assertEquals(expected[1][0], GelmanRubinConvergence.diagnose(d_results, d_parameters[1], 500), EPSILON);
		assertEquals(expected[2][0], GelmanRubinConvergence.diagnose(d_results, d_parameters[2], 500), EPSILON);
	}
	
	@Test @Ignore
	public void testResults2k() throws IOException {
		double[][] expected = readExpected("conv-2k.txt");
		assertEquals(expected[0][0], GelmanRubinConvergence.diagnose(d_results, d_parameters[0], 2000), EPSILON);
		assertEquals(expected[1][0], GelmanRubinConvergence.diagnose(d_results, d_parameters[1], 2000), EPSILON);
		assertEquals(expected[2][0], GelmanRubinConvergence.diagnose(d_results, d_parameters[2], 2000), EPSILON);
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
}