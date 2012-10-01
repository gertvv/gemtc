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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.drugis.mtc.Parameter;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.parameterization.RandomEffectsStandardDeviation;
import org.drugis.mtc.test.FileResults;
import org.junit.Before;
import org.junit.Test;

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
				new BasicParameter(t1, t2), new BasicParameter(t2, t3), new RandomEffectsStandardDeviation()	
		};
		d_results = new FileResults(is, d_parameters, 3, 10000);
		d_results.makeSamplesAvailable();
	}
	
	@Test
	public void testResults500() throws IOException {
		double[][] expected = readExpected("conv-0.5k.txt");
		assertEquals(expected[0][0], GelmanRubinConvergence.diagnose(d_results, d_parameters[0], 500), EPSILON);
		assertEquals(expected[1][0], GelmanRubinConvergence.diagnose(d_results, d_parameters[1], 500), EPSILON);
		assertEquals(expected[2][0], GelmanRubinConvergence.diagnose(d_results, d_parameters[2], 500), EPSILON);
	}
	
	@Test
	public void testResults2k() throws IOException {
		double[][] expected = readExpected("conv-2k.txt");
		assertEquals(expected[0][0], GelmanRubinConvergence.diagnose(d_results, d_parameters[0], 2000), EPSILON);
		assertEquals(expected[1][0], GelmanRubinConvergence.diagnose(d_results, d_parameters[1], 2000), EPSILON);
		assertEquals(expected[2][0], GelmanRubinConvergence.diagnose(d_results, d_parameters[2], 2000), EPSILON);
	}
	
	@Test
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
