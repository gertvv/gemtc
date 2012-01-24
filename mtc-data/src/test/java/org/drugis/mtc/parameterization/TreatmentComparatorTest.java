package org.drugis.mtc.parameterization;

import static org.junit.Assert.*;

import org.drugis.mtc.model.Treatment;
import org.junit.Test;

public class TreatmentComparatorTest {
	@Test
	public void testCompare() {
		TreatmentComparator tc = TreatmentComparator.INSTANCE;
		assertTrue(tc.compare(new Treatment("A"), new Treatment("B")) < 0);
		assertTrue(tc.compare(new Treatment("B"), new Treatment("A")) > 0);
		assertTrue(tc.compare(new Treatment("A"), new Treatment("A")) == 0);
		assertTrue(tc.compare(new Treatment("A"), new Treatment("A", "Description")) == 0);
	}
}
