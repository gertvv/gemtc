package org.drugis.mtc.model;

import static org.junit.Assert.assertEquals;

import org.drugis.common.JUnitUtil;
import org.junit.Test;

public class TreatmentTest {
	@Test
	public void testEquals() {
		assertEquals(new Treatment("A"), new Treatment("A"));
		assertEquals(new Treatment("A", ""), new Treatment("A"));
		assertEquals(new Treatment("A").hashCode(), new Treatment("A").hashCode());
		JUnitUtil.assertNotEquals(new Treatment("B"), new Treatment("A"));
	}
}
