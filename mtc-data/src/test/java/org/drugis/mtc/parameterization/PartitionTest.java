package org.drugis.mtc.parameterization;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

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
}
