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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.drugis.mtc.model.Treatment;
import org.junit.Before;
import org.junit.Test;

import edu.uci.ics.jung.graph.util.Pair;

public class ThetaArgumentMakerTest {
	
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

	@Test
	public void testGetArgumentTwoArm() {
		List<Treatment> treatments = Arrays.asList(d_ta, d_tb);
		List<List<Pair<Treatment>>> pmtz = Collections.singletonList(
				Collections.singletonList(new Pair<Treatment>(d_ta, d_tb)));
		
		double[] mu = {1.0};
		double[] delta = {-1.0};
		double[][] data = { mu, delta };
		double[] expected = {1.0, 0.0};
		
		ThetaArgumentMaker maker = new ThetaArgumentMaker(treatments, pmtz, 0, 1);
		assertArrayEquals(expected, maker.getArgument(data), EPSILON);
		
		mu[0] = -0.5;
		delta[0] = 0.9;
		expected[0] = -0.5;
		expected[1] = 0.4;
		assertArrayEquals(expected, maker.getArgument(data), EPSILON);
	}
	
	@Test
	public void testGetArgumentTwoArmReverse() {
		List<Treatment> treatments = Arrays.asList(d_ta, d_tb);
		List<List<Pair<Treatment>>> pmtz = Collections.singletonList(
				Collections.singletonList(new Pair<Treatment>(d_tb, d_ta)));
		
		double[] mu = {1.0};
		double[] delta = {-1.0};
		double[][] data = { mu, delta };
		double[] expected = {0.0, 1.0};
		
		ThetaArgumentMaker maker = new ThetaArgumentMaker(treatments, pmtz, 0, 1);
		assertArrayEquals(expected, maker.getArgument(data), EPSILON);
		
		mu[0] = -0.5;
		delta[0] = 0.9;
		expected[0] = 0.4;
		expected[1] = -0.5;
		assertArrayEquals(expected, maker.getArgument(data), EPSILON);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetArgumentThreeArm() {
		List<Treatment> treatments = Arrays.asList(d_tb, d_tc, d_td);
		List<List<Pair<Treatment>>> pmtz = Collections.singletonList(
				Arrays.asList(new Pair<Treatment>(d_tc, d_td), new Pair<Treatment>(d_tc, d_tb)));
		
		double[] mu = {2.0};
		double[] delta = {3.0, 2.0};
		double[][] data = { mu, delta };
		double[] expected = {4.0, 2.0, 5.0};
		
		ThetaArgumentMaker maker = new ThetaArgumentMaker(treatments, pmtz, 0, 1);
		assertArrayEquals(expected, maker.getArgument(data), EPSILON);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetArgumentSplit() {
		List<Treatment> treatments = Arrays.asList(d_tb, d_tc, d_td);
		List<List<Pair<Treatment>>> pmtz = Arrays.asList(
				Collections.singletonList(new Pair<Treatment>(d_tc, d_td)),
				Collections.singletonList(new Pair<Treatment>(d_tc, d_tb)));
		
		double[] mu = {2.0};
		double[] delta = {3.0, 2.0};
		double[][] data = { mu, delta };
		double[] expected = {4.0, 2.0, 5.0};
		
		ThetaArgumentMaker maker = new ThetaArgumentMaker(treatments, pmtz, 0, 1);
		assertArrayEquals(expected, maker.getArgument(data), EPSILON);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetArgumentSplit2() {
		List<Treatment> treatments = Arrays.asList(d_tb, d_tc, d_td);
		List<List<Pair<Treatment>>> pmtz = Arrays.asList(
				Collections.singletonList(new Pair<Treatment>(d_tb, d_tc)),
				Collections.singletonList(new Pair<Treatment>(d_tc, d_td)));
		
		double[] mu = {2.0};
		double[] delta = {3.0, 2.0};
		double[][] data = { mu, delta };
		double[] expected = {2.0, 5.0, 7.0};
		
		ThetaArgumentMaker maker = new ThetaArgumentMaker(treatments, pmtz, 0, 1);
		assertArrayEquals(expected, maker.getArgument(data), EPSILON);
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=IllegalArgumentException.class)
	public void testDisconnectedTree() {
		List<Treatment> treatments = Arrays.asList(d_ta, d_tb, d_tc, d_td);
		List<List<Pair<Treatment>>> pmtz = Arrays.asList(
				Collections.singletonList(new Pair<Treatment>(d_ta, d_tb)),
				Collections.singletonList(new Pair<Treatment>(d_tc, d_td)));
		
		new ThetaArgumentMaker(treatments, pmtz, 0, 1);
	}
}