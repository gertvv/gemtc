package org.drugis.mtc.parameterization;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;

import org.drugis.common.JUnitUtil;
import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.junit.Before;
import org.junit.Test;

public class PartTest {
	private Treatment d_ta;
	private Treatment d_tb;
	private Treatment d_tc;
	private Study d_s1;
	private Study d_s2;
	private Study d_s3;
	
	@Before
	public void setUp() {
		d_ta = new Treatment("A");
		d_tb = new Treatment("B");
		d_tc = new Treatment("C");
		d_s1 = new Study("1");
		d_s1.getMeasurements().add(new Measurement(d_ta));
		d_s1.getMeasurements().add(new Measurement(d_tb));
		d_s2 = new Study("2");
		d_s2.getMeasurements().add(new Measurement(d_ta));
		d_s2.getMeasurements().add(new Measurement(d_tc));
		d_s3 = new Study("3");
		d_s3.getMeasurements().add(new Measurement(d_ta));
		d_s3.getMeasurements().add(new Measurement(d_tb));
		d_s3.getMeasurements().add(new Measurement(d_tc));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPreconditionStudiesNotEmpty() {
		new Part(d_ta, d_tb, new HashSet<Study>());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testPreconditionStudiesIncludeTreatments() {
		new Part(d_ta, d_tb, new HashSet<Study>(Arrays.asList(d_s2)));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testPreconditionAllStudiesIncludeTreatments() {
		new Part(d_ta, d_tb, new HashSet<Study>(Arrays.asList(d_s1, d_s2)));
	}

	@Test
	public void testPreconditionPass() {
		new Part(d_ta, d_tb, new HashSet<Study>(Arrays.asList(d_s1)));
		new Part(d_ta, d_tc, new HashSet<Study>(Arrays.asList(d_s2, d_s3)));
	}
	
	@Test
	public void testEquals() {
		assertEquals(new Part(d_ta, d_tb, new HashSet<Study>(Arrays.asList(d_s1))), new Part(d_ta, d_tb, new HashSet<Study>(Arrays.asList(d_s1))));
		assertEquals(new Part(d_ta, d_tb, new HashSet<Study>(Arrays.asList(d_s1))).hashCode(), new Part(d_ta, d_tb, new HashSet<Study>(Arrays.asList(d_s1))).hashCode());
		assertEquals(new Part(d_ta, d_tb, new HashSet<Study>(Arrays.asList(d_s1))), new Part(d_tb, d_ta, new HashSet<Study>(Arrays.asList(d_s1))));
		assertEquals(new Part(d_ta, d_tb, new HashSet<Study>(Arrays.asList(d_s1))).hashCode(), new Part(d_tb, d_ta, new HashSet<Study>(Arrays.asList(d_s1))).hashCode());
		JUnitUtil.assertNotEquals(new Part(d_ta, d_tb, new HashSet<Study>(Arrays.asList(d_s1))), new Part(d_ta, d_tb, new HashSet<Study>(Arrays.asList(d_s3))));
	}
}
