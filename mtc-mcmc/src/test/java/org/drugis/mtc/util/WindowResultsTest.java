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

package org.drugis.mtc.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.convergence.GelmanRubinAcceptanceTest;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.parameterization.RandomEffectsVariance;
import org.drugis.mtc.test.FileResults;
import org.junit.Before;
import org.junit.Test;

public class WindowResultsTest {
	private static final double EPSILON = 0.0000001;
	private WindowResults d_results;
	private Parameter[] d_parameters;
	private FileResults d_fileResults;
	private int d_start;
	private int d_end;


	private double[] getSamples(MCMCResults r, int p, int c) {
		double[] samples = new double[r.getNumberOfSamples()];
		for (int i = 0; i < samples.length; ++i) {
			samples[i] = r.getSample(p, c, i);
		}
		return samples;
	}
    
	@Before
	public void setup() throws IOException {
		InputStream is = GelmanRubinAcceptanceTest.class.getResourceAsStream("conv-samples.txt");
		Treatment t1 = new Treatment("iPCI");
		Treatment t2 = new Treatment("mPCI");
		Treatment t3 = new Treatment("sPCI");
		d_parameters = new Parameter[] {
				new BasicParameter(t1, t2), new BasicParameter(t2, t3), new RandomEffectsVariance()	
		};	
		d_fileResults = new FileResults(is, d_parameters, 3, 10000);
		d_fileResults.makeSamplesAvailable();
		d_start = 100;
		d_end = 150;
	}
	
	@Test
	public void testCreation() {
		d_results = new WindowResults(d_fileResults, d_start, d_end);
	}

	@Test
	public void testDataShouldBeEqual() {
		d_results = new WindowResults(d_fileResults, d_start, d_end);
		double[] samples;
		double[] tempArray = new double[50];
		for(Parameter par: d_fileResults.getParameters()) {
			int p = d_fileResults.findParameter(par);
			for(int i = 0; i < d_fileResults.getNumberOfChains(); ++i) {
				samples = getSamples(d_fileResults, p, i);
				System.arraycopy(samples, 100, tempArray, 0, 50);
				assertArrayEquals(tempArray, getSamples(d_results, d_results.findParameter(par), i), EPSILON);
			}
		}
	}
	
	@Test
	public void testNChains() {
		d_results = new WindowResults(d_fileResults, d_start, d_end);
		assertEquals(d_fileResults.getNumberOfChains(), d_results.getNumberOfChains());
	}
	
	@Test
	public void testNSamples() {
		d_results = new WindowResults(d_fileResults, d_start, d_end);
		assertEquals(d_end-d_start, d_results.getNumberOfSamples());
	}

}
