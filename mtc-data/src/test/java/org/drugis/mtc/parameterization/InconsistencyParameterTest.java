package org.drugis.mtc.parameterization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;

import org.drugis.mtc.model.Treatment;
import org.junit.Test;

public class InconsistencyParameterTest {
	@Test
	public void testGetName() {
		List<Treatment> cycle = Arrays.asList(new Treatment("A"), new Treatment("B"), new Treatment("C"), new Treatment("A"));
		InconsistencyParameter parameter = new InconsistencyParameter(cycle);
		
		assertEquals("w.A.B.C", parameter.getName());
		assertEquals(parameter.getName(), parameter.toString());
	}
	
	@Test
	public void testEquals() {
		List<Treatment> cycle1 = Arrays.asList(new Treatment("A"), new Treatment("B"), new Treatment("C"), new Treatment("A"));
		List<Treatment> cycle2 = Arrays.asList(new Treatment("A"), new Treatment("C"), new Treatment("B"), new Treatment("A"));
		InconsistencyParameter parameter = new InconsistencyParameter(cycle1);
		
		assertEquals(parameter, new InconsistencyParameter(cycle1));
		assertEquals(parameter.hashCode(), new InconsistencyParameter(cycle1).hashCode());
		assertFalse(parameter.equals(new InconsistencyParameter(cycle2)));
	}
}
