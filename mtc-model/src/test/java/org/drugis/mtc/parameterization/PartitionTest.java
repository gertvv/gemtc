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

package org.drugis.mtc.parameterization;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.drugis.common.JUnitUtil;
import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.junit.Before;
import org.junit.Test;

public class PartitionTest {
	private Part d_p1;
	private Part d_p2;
	private Part d_p3;
	private Part d_p4;
	private Part d_p5;
	private Part d_p6;
	private Part d_p7;
	private Part d_p8;
	
	@Before
	public void setUp() {
		Treatment ta = new Treatment("A");
		Treatment tb = new Treatment("B");
		Treatment tc = new Treatment("C");
		Study s1 = new Study("1");
		s1.getMeasurements().add(new Measurement(ta));
		s1.getMeasurements().add(new Measurement(tb));
		Study s2 = new Study("2");
		s2.getMeasurements().add(new Measurement(tb));
		s2.getMeasurements().add(new Measurement(tc));
		Study s3 = new Study("3");
		s3.getMeasurements().add(new Measurement(ta));
		s3.getMeasurements().add(new Measurement(tb));
		s3.getMeasurements().add(new Measurement(tc));
		Study s4 = new Study("4");
		s4.getMeasurements().add(new Measurement(ta));
		s4.getMeasurements().add(new Measurement(tb));
		s4.getMeasurements().add(new Measurement(tc));
		
		d_p1 = new Part(ta, ta, Collections.singleton(s3));
		d_p2 = new Part(ta, tb, Collections.singleton(s3));
		d_p3 = new Part(ta, tb, Collections.singleton(s4));
		d_p4 = new Part(ta, tc, Collections.singleton(s4));
		d_p5 = new Part(tb, tc, Collections.singleton(s4));
		d_p6 = new Part(ta, ta, Collections.singleton(s4));
		d_p7 = new Part(ta, tb, new HashSet<Study>(Arrays.asList(s1, s4)));
		d_p8 = new Part(tc, tb, new HashSet<Study>(Arrays.asList(s2, s4)));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPreconditionCycle1() {
		new Partition(Collections.singleton(d_p2));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testPreconditionCycle2() {
		new Partition(new HashSet<Part>(Arrays.asList(d_p3, d_p4)));
	}
	
	@Test
	public void testPreconditionPass() {
		new Partition(Collections.singleton(d_p1));
		new Partition(new HashSet<Part>(Arrays.asList(d_p2, d_p3)));
		new Partition(new HashSet<Part>(Arrays.asList(d_p3, d_p4, d_p5)));
	}
	
	@Test
	public void testEquals() {
		assertEquals(new Partition(Collections.singleton(d_p1)), new Partition(Collections.singleton(d_p1)));
		assertEquals(new Partition(Collections.singleton(d_p1)).hashCode(), new Partition(Collections.singleton(d_p1)).hashCode());
		JUnitUtil.assertNotEquals(new Partition(Collections.singleton(d_p1)), new Partition(Arrays.asList(d_p2, d_p3)));
		JUnitUtil.assertNotEquals(new Partition(Collections.singleton(d_p1)), new Partition(Collections.singleton(d_p6)));
	}
	
	@Test
	public void testReduce() {
		// Check that a partition that reduces to a point works correctly
		assertEquals(new Partition(Collections.singleton(d_p6)), new Partition(Arrays.asList(d_p3, d_p4, d_p5)).reduce());

		// Partitions that reduce to two sets of studies on the same edge
		assertEquals(new Partition(Arrays.asList(d_p7, d_p3)), new Partition(Arrays.asList(d_p7, d_p4, d_p5)).reduce());
		assertEquals(new Partition(Arrays.asList(d_p8, d_p5)), new Partition(Arrays.asList(d_p3, d_p8, d_p4)).reduce());

		// Partition that does not reduce
		assertEquals(new Partition(Arrays.asList(d_p7, d_p8, d_p4)), new Partition(Arrays.asList(d_p7, d_p8, d_p4)).reduce());
	}
	
	@Test
	public void testLongerCycle() {
		// This test was added because longer cycles were causing reduce() to go into an infite loop
		Treatment ta = new Treatment("A");
		Treatment tb = new Treatment("B");
		Treatment tc = new Treatment("C");
		Treatment td = new Treatment("D");
		
		Study s1 = new Study("1");
		s1.getMeasurements().addAll(Arrays.asList(new Measurement(ta), new Measurement(tb)));
		Study s2 = new Study("2");
		s2.getMeasurements().addAll(Arrays.asList(new Measurement(tb), new Measurement(tc), new Measurement(td)));
		Study s3 = new Study("3");
		s3.getMeasurements().addAll(Arrays.asList(new Measurement(ta), new Measurement(td)));
		Study s4 = new Study("4");
		s4.getMeasurements().addAll(Arrays.asList(new Measurement(tc), new Measurement(td)));
		
		Part p1 = new Part(ta, tb, Arrays.asList(s1));
		Part p2 = new Part(tb, tc, Arrays.asList(s2));
		Part p3 = new Part(tc, td, Arrays.asList(s2, s4));
		Part p4 = new Part(td, ta, Arrays.asList(s3));
		
		Partition partition = new Partition(Arrays.asList(p1, p2, p3, p4));
		assertEquals(partition, partition.reduce());
	}
}
