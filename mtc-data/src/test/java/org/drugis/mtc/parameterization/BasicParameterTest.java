package org.drugis.mtc.parameterization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.drugis.mtc.model.Treatment;
import org.junit.Test;

public class BasicParameterTest {
	@Test
	public void testGetName() {
		Treatment t1 = new Treatment("A_1");
		Treatment t2 = new Treatment("B52");
		assertEquals("d.A_1.B52", new BasicParameter(t1, t2).getName());
	}
	
	@Test
	public void testEquals() {
		Treatment t1 = new Treatment("A_1");
		Treatment t2 = new Treatment("B52");
		Treatment t3 = new Treatment("C");
		
		assertEquals(new BasicParameter(t1, t2), new BasicParameter(t1, t2));
		assertEquals(new BasicParameter(t1, t2).hashCode(), new BasicParameter(t1, t2).hashCode());
		assertFalse(new BasicParameter(t1, t2).equals(new BasicParameter(t3, t2)));
		assertFalse(new BasicParameter(t1, t2).equals(new BasicParameter(t1, t3)));
	}
}
