package org.drugis.mtc.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StudyTest {
	@Test
	public void testContainsTreatment() {
		Study s1 = new Study("1");
		s1.getMeasurements().add(new Measurement(new Treatment("A")));
		s1.getMeasurements().add(new Measurement(new Treatment("B")));
		s1.getMeasurements().add(new Measurement(new Treatment("C")));
		assertTrue(s1.containsTreatment(new Treatment("A")));
		assertTrue(s1.containsTreatment(new Treatment("B")));
		assertTrue(s1.containsTreatment(new Treatment("C")));
		assertFalse(s1.containsTreatment(new Treatment("D")));
	}
}
