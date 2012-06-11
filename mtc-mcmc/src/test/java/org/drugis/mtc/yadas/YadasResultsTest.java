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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import gov.lanl.yadas.MCMCParameter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class YadasResultsTest {
	private BasicParameter d_param1;
	private BasicParameter d_param2;
	private BasicParameter d_param3;
	private Derivation d_deriv;
	private BasicParameter d_paramx;
	private YadasResults d_results;
	
    @Rule
    public ExpectedException d_thrown = ExpectedException.none();
	
    @Before
	public void setUp() {
		Treatment ta = new Treatment("A");
		Treatment tb = new Treatment("B");
		Treatment tc = new Treatment("C");
		d_param1 = new BasicParameter(ta, tb);
		d_param2 = new BasicParameter(tb, tc);
		d_param3 = new BasicParameter(ta, tc);
		Map<Parameter, Integer> map = new HashMap<Parameter, Integer>();
		map.put(d_param1, 1);
		map.put(d_param2, 1);
		d_deriv = new Derivation(map);
		d_paramx = new BasicParameter(ta, new Treatment("X"));
		
		d_results = new YadasResults();
		d_results.setNumberOfChains(1);
		d_results.setNumberOfIterations(10);
		d_results.setDirectParameters(Arrays.<Parameter>asList(d_param1, d_param2));
		d_results.setDerivedParameters(Collections.<Parameter,Derivation>singletonMap(d_param3, d_deriv));
	}
    
    @Test
    public void testOtherInitializerOrder() {
		d_results = new YadasResults();
		d_results.setNumberOfIterations(10);
		d_results.setNumberOfChains(1);
    }

	@Test 
	public void testGetters() {
		assertArrayEquals(new Parameter[] {d_param1, d_param2}, d_results.getParameters());
		assertEquals(0, d_results.findParameter(d_param1));
		assertEquals(1, d_results.findParameter(d_param2));
		assertEquals(-1, d_results.findParameter(d_paramx));
		assertEquals(2, d_results.findParameter(d_param3));
		assertEquals(1, d_results.getNumberOfChains());
		assertEquals(0, d_results.getNumberOfSamples());
		
		d_results.simulationFinished();
		assertEquals(10, d_results.getNumberOfSamples());
		assertEquals(0.0, d_results.getSample(0, 0, 0), 0.0);

		d_results.getSample(0, 0, 9);
		d_results.getSample(1, 0, 9);
		d_results.getSample(2, 0, 9);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void testIterationOutOfBounds() {
		d_results.simulationFinished();
		d_results.getSample(0, 0, 10);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void testChainOutOfBounds() {
		d_results.simulationFinished();
		d_results.getSample(0, 1, 3);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void testParameterOutOfBounds() {
		d_results.simulationFinished();
		d_results.getSample(3, 0, 3);
	}
	
	@Test
	public void testWriters() {
		MCMCParameter mcmcParam = new MCMCParameter(
			new double[] {0.0, 1.0},
			new double[] {0.1, 0.1}, "name");
		ParameterWriter writer = d_results.getParameterWriter(d_param2, 0, mcmcParam, 1);
		for (int i = 0; i < 10; ++i) {
			mcmcParam.setValue(new double[]{0.0, i});
			writer.output();
		}
		d_results.simulationFinished();
		for (int i = 0; i < 10; ++i) {
			assertEquals(i, d_results.getSample(1, 0, i), 0.0);
		}
	}

	@Test 
	public void testAdditionalIterationsPreservesSamples() {
		MCMCParameter mcmcParam = new MCMCParameter(
				new double[] {0.0, 1.0},
				new double[] {0.1, 0.1}, "name");
		ParameterWriter writer = d_results.getParameterWriter(d_param2, 0, mcmcParam, 1);
		for (int i = 0; i < 10; ++i) {
			mcmcParam.setValue(new double[]{0.0, i});
			writer.output();
		}
		d_results.simulationFinished();

		d_results.setNumberOfIterations(20);
		d_results.simulationFinished();

		for (int i = 0; i < 10; ++i) {
			assertEquals(i, d_results.getSample(1, 0, i), 0.0);
		}
		for (int i = 10; i < 20; ++i) {
			assertEquals(0.0, d_results.getSample(1, 0, i), 0.0);
		}
	}

	@Test
	public void testDerivedSamples() {
		MCMCParameter mcmcParam = new MCMCParameter(
				new double[] {0.0, 1.0},
				new double[] {0.1, 0.1}, "name");
		ParameterWriter writer1 = d_results.getParameterWriter(d_param1, 0, mcmcParam, 0);
		ParameterWriter writer2 = d_results.getParameterWriter(d_param2, 0, mcmcParam, 1);
		for (int i = 0; i < 10; ++i) {
			mcmcParam.setValue(new double[]{i + 2, i});
			writer1.output();
			writer2.output();
		}
		d_results.simulationFinished();

		for (int i = 0; i < 10; ++i) {
			assertEquals(2 * (i + 1), d_results.getSample(2, 0, i), 0.0);
		}
	}

	@Test
	public void testEvent() {
		MCMCResultsListener mock = EasyMock.createStrictMock(MCMCResultsListener.class);
		mock.resultsEvent((MCMCResultsEvent)EasyMock.anyObject());
		EasyMock.replay(mock);
		
		d_results.addResultsListener(mock);
		d_results.simulationFinished();
		EasyMock.verify(mock);
	}
}