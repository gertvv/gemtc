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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.drugis.mtc.model.Treatment;
import org.junit.Before;
import org.junit.Test;

import edu.uci.ics.jung.graph.util.Pair;
import gov.lanl.yadas.ArgumentMaker;

public class SigmaRowArgumentMakerTest {
	private static final double EPSILON = 0.000000001;

	private Treatment d_ta;
	private Treatment d_tb;
	private Treatment d_tc;
	private Treatment d_td;

	@Before
	public void setUp() { 
		d_ta = new Treatment("A");
		d_tb = new Treatment("B");
		d_tc = new Treatment("C");
		d_td = new Treatment("D");
	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetArgumentThreeArm() {
		double[] sigma = new double[] {2.0};
		double[] expected0 = new double[] {4.0, 2.0};
		double[] expected1 = new double[] {2.0, 4.0};
		
		List<List<Pair<Treatment>>> param = Collections.singletonList(Arrays.asList(
				new Pair<Treatment>(d_ta, d_tb), new Pair<Treatment>(d_ta, d_tc)));
		
		SigmaRowArgumentMaker maker0 = new SigmaRowArgumentMaker(param, 0, 0);
		assertArrayEquals(expected0, maker0.getArgument(new double[][] {sigma}), EPSILON);
		SigmaRowArgumentMaker maker0b = new SigmaRowArgumentMaker(param, 0, 1);
		assertArrayEquals(expected0, maker0b.getArgument(new double[][] {null, sigma}), EPSILON);

		SigmaRowArgumentMaker maker1 = new SigmaRowArgumentMaker(param, 1, 0);
		assertArrayEquals(expected1, maker1.getArgument(new double[][] {sigma}), EPSILON);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetArgumentFourArm() {
		double[] sigma = new double[] {2.0};
		double[] expected0 = new double[] {4.0, 2.0, 2.0};
		double[] expected1 = new double[] {2.0, 4.0, 2.0};
		
		List<List<Pair<Treatment>>> param = Collections.singletonList(Arrays.asList(
				new Pair<Treatment>(d_ta, d_tb), new Pair<Treatment>(d_ta, d_tc), new Pair<Treatment>(d_ta, d_td)));
		
		SigmaRowArgumentMaker maker0 = new SigmaRowArgumentMaker(param, 0, 0);
		assertArrayEquals(expected0, maker0.getArgument(new double[][] {sigma}), EPSILON);

		SigmaRowArgumentMaker maker1 = new SigmaRowArgumentMaker(param, 1, 0);
		assertArrayEquals(expected1, maker1.getArgument(new double[][] {sigma}), EPSILON);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetArgumentThreeArmSplit() {
		double[] sigma = new double[] {2.0};
		double[] expected0 = new double[] {4.0, 0.0};
		double[] expected1 = new double[] {0.0, 4.0};
		
		List<List<Pair<Treatment>>> param = Arrays.asList(
				Arrays.asList(new Pair<Treatment>(d_ta, d_tb)),
				Arrays.asList(new Pair<Treatment>(d_ta, d_tc)));
		
		SigmaRowArgumentMaker maker0 = new SigmaRowArgumentMaker(param, 0, 0);
		assertArrayEquals(expected0, maker0.getArgument(new double[][] {sigma}), EPSILON);
		SigmaRowArgumentMaker maker0b = new SigmaRowArgumentMaker(param, 0, 1);
		assertArrayEquals(expected0, maker0b.getArgument(new double[][] {null, sigma}), EPSILON);

		SigmaRowArgumentMaker maker1 = new SigmaRowArgumentMaker(param, 1, 0);
		assertArrayEquals(expected1, maker1.getArgument(new double[][] {sigma}), EPSILON);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetArgumentFourArmSplit() {
		double[] sigma = new double[] {2.0};
		double[] expected0 = new double[] {4.0, 2.0, 0.0};
		double[] expected1 = new double[] {2.0, 4.0, 0.0};
		double[] expected2 = new double[] {0.0, 0.0, 4.0};
		
		List<List<Pair<Treatment>>> param = Arrays.asList(
				Arrays.asList(new Pair<Treatment>(d_ta, d_tb), new Pair<Treatment>(d_ta, d_tc)),
				Arrays.asList(new Pair<Treatment>(d_ta, d_td)));
		
		SigmaRowArgumentMaker maker0 = new SigmaRowArgumentMaker(param, 0, 0);
		assertArrayEquals(expected0, maker0.getArgument(new double[][] {sigma}), EPSILON);

		SigmaRowArgumentMaker maker1 = new SigmaRowArgumentMaker(param, 1, 0);
		assertArrayEquals(expected1, maker1.getArgument(new double[][] {sigma}), EPSILON);
		
		SigmaRowArgumentMaker maker2 = new SigmaRowArgumentMaker(param, 2, 0);
		assertArrayEquals(expected2, maker2.getArgument(new double[][] {sigma}), EPSILON);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testMatrixMaker() {
		double[] sigma = new double[] {2.0};
		double[] expected0 = new double[] {4.0, 2.0, 0.0};
		double[] expected1 = new double[] {2.0, 4.0, 0.0};
		double[] expected2 = new double[] {0.0, 0.0, 4.0};
		
		List<List<Pair<Treatment>>> param = Arrays.asList(
				Arrays.asList(new Pair<Treatment>(d_ta, d_tb), new Pair<Treatment>(d_ta, d_tc)),
				Arrays.asList(new Pair<Treatment>(d_ta, d_td)));
		
		List<ArgumentMaker> makers = SigmaRowArgumentMaker.createMatrixArgumentMaker(param, 0);
		assertEquals(3, makers.size());
		assertArrayEquals(expected0, makers.get(0).getArgument(new double[][] {sigma}), EPSILON);
		assertArrayEquals(expected1, makers.get(1).getArgument(new double[][] {sigma}), EPSILON);
		assertArrayEquals(expected2, makers.get(2).getArgument(new double[][] {sigma}), EPSILON);
	}
}