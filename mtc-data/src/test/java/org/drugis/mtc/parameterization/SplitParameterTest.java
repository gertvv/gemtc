package org.drugis.mtc.parameterization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.drugis.mtc.model.Treatment;
import org.junit.Test;

public class SplitParameterTest {
	@Test
	public void testGetName() {
		Treatment t1 = new Treatment("A_1");
		Treatment t2 = new Treatment("B52");
		assertEquals("d.A_1.B52.dir", new SplitParameter(t1, t2, true).getName());
		assertEquals("d.A_1.B52.ind", new SplitParameter(t1, t2, false).getName());
	}
	
	@Test
	public void testEquals() {
		Treatment t1 = new Treatment("A_1");
		Treatment t2 = new Treatment("B52");
		Treatment t3 = new Treatment("C");
		
		assertEquals(new SplitParameter(t1, t2, true), new SplitParameter(t1, t2, true));
		assertEquals(new SplitParameter(t1, t2, true).hashCode(), new SplitParameter(t1, t2, true).hashCode());
		assertFalse(new SplitParameter(t1, t2, true).equals(new SplitParameter(t3, t2, true)));
		assertFalse(new SplitParameter(t1, t2, true).equals(new SplitParameter(t1, t3, true)));
		assertFalse(new SplitParameter(t1, t2, true).equals(new SplitParameter(t1, t2, false)));
	}
	
	@Test
	public void testCompare() {
		Treatment t1 = new Treatment("A_1");
		Treatment t2 = new Treatment("B52");
		Treatment t3 = new Treatment("C");

		assertTrue(new SplitParameter(t1, t2, true).compareTo(new SplitParameter(t1, t2, true)) == 0);

		assertTrue(new SplitParameter(t1, t2, true).compareTo(new SplitParameter(t1, t2, false)) < 0);
		assertTrue(new SplitParameter(t1, t2, false).compareTo(new SplitParameter(t1, t2, true)) > 0);
		
		assertTrue(new SplitParameter(t3, t2, true).compareTo(new SplitParameter(t1, t2, true)) > 0);
		assertTrue(new SplitParameter(t2, t1, true).compareTo(new SplitParameter(t1, t2, true)) > 0);
	}
}
