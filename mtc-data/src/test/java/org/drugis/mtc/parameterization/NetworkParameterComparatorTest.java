package org.drugis.mtc.parameterization;

import static org.junit.Assert.assertTrue;

import org.drugis.mtc.model.Treatment;
import org.junit.Test;

public class NetworkParameterComparatorTest {
	@Test
	public void testCompareBasicParameters() {
		NetworkParameterComparator pc = new NetworkParameterComparator();
		
		Treatment ta = new Treatment("A");
		Treatment tb = new Treatment("B");
		Treatment tc = new Treatment("C");
		
		assertTrue(pc.compare(new BasicParameter(ta, tb), new BasicParameter(tc, tb)) < 0);
		assertTrue(pc.compare(new BasicParameter(tc, tb), new BasicParameter(ta, tb)) > 0);
		assertTrue(pc.compare(new BasicParameter(ta, tb), new BasicParameter(tc, ta)) < 0);
		assertTrue(pc.compare(new BasicParameter(ta, tb), new BasicParameter(ta, tb)) == 0);
		assertTrue(pc.compare(new BasicParameter(ta, tb), new BasicParameter(ta, tc)) < 0);
		assertTrue(pc.compare(new BasicParameter(ta, tc), new BasicParameter(ta, ta)) > 0);
	}
}
