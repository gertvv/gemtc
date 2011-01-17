/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
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

import org.drugis.mtc.BasicParameter;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.Treatment;
import org.drugis.mtc.convergence.GelmanRubinAcceptanceTest;
import org.drugis.mtc.test.FileResults;
import org.drugis.mtc.yadas.RandomEffectsVariance;
import org.junit.Before;
import org.junit.Test;

public class WindowResultsTest {
	private static final double EPSILON = 0.0000001;
	private WindowResults d_results;
	private Parameter[] d_parameters;
	private FileResults d_FileResults;
	private int d_start;
	private int d_end;
    
	@Before
	public void setup() throws IOException {
		InputStream is = GelmanRubinAcceptanceTest.class.getResourceAsStream("conv-samples.txt");
		Treatment t1 = new Treatment("iPCI");
		Treatment t2 = new Treatment("mPCI");
		Treatment t3 = new Treatment("sPCI");
		d_parameters = new Parameter[] {
				new BasicParameter(t1, t2), new BasicParameter(t2, t3), new RandomEffectsVariance()	
		};	
		d_FileResults = new FileResults(is, d_parameters, 3, 10000);
		d_FileResults.makeSamplesAvailable();
		d_start = 100;
		d_end = 150;
	}
	
	@Test
	public void testCreation() {
		d_results = new WindowResults(d_FileResults, d_start, d_end);
	}

	@Test
	public void testDataShouldBeEqual() {
		d_results = new WindowResults(d_FileResults, d_start, d_end);
		double[] samples;
		double[] tempArray = new double[50];
		for(Parameter par: d_FileResults.getParameters()) {
			int p = d_FileResults.findParameter(par);
			for(int i = 0; i < d_FileResults.getNumberOfChains(); ++i) {
				samples = d_FileResults.getSamples(p, i);
				System.arraycopy(samples, 100, tempArray, 0, 50);
				assertArrayEquals(tempArray, d_results.getSamples(d_results.findParameter(par), i), EPSILON);
			}
		}
	}
	
	@Test
	public void testNChains() {
		d_results = new WindowResults(d_FileResults, d_start, d_end);
		assertEquals(d_FileResults.getNumberOfChains(), d_results.getNumberOfChains());
	}
	
	@Test
	public void testNSamples() {
		d_results = new WindowResults(d_FileResults, d_start, d_end);
		assertEquals(d_end-d_start, d_results.getNumberOfSamples());
	}

}
