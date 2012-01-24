package org.drugis.mtc.parameterization;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.drugis.mtc.model.Treatment;
import org.junit.Test;

public class NetworkParameterComparatorTest {
	@Test
	public void testCompareBasicParameters() {
		NetworkParameterComparator pc = NetworkParameterComparator.INSTANCE;
		
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
	
	@Test
	public void testCompareInconsistencyParameters() {
		NetworkParameterComparator pc = NetworkParameterComparator.INSTANCE;
		
		Treatment ta = new Treatment("A");
		Treatment tb = new Treatment("B");
		Treatment tc = new Treatment("C");
		Treatment td = new Treatment("D");
		
		List<Treatment> c1 = Arrays.asList(ta, tb, tc, ta);
		List<Treatment> c2 = Arrays.asList(ta, tb, td, ta);
		List<Treatment> c3 = Arrays.asList(ta, tb, tc, td, ta);
		
		assertTrue(pc.compare(new InconsistencyParameter(c1), new InconsistencyParameter(c2)) < 0);
		assertTrue(pc.compare(new InconsistencyParameter(c1), new InconsistencyParameter(c1)) == 0);
		assertTrue(pc.compare(new InconsistencyParameter(c2), new InconsistencyParameter(c1)) > 0);
		assertTrue(pc.compare(new InconsistencyParameter(c1), new InconsistencyParameter(c3)) < 0);
	}
	
	@Test
	public void testCompareMixed() {
		NetworkParameterComparator pc = NetworkParameterComparator.INSTANCE;
		
		Treatment ta = new Treatment("A");
		Treatment tb = new Treatment("B");
		Treatment tc = new Treatment("C");
		
		List<Treatment> c1 = Arrays.asList(ta, tb, tc, ta);
		
		assertTrue(pc.compare(new BasicParameter(ta, tb), new InconsistencyParameter(c1)) < 0);
		assertTrue(pc.compare(new InconsistencyParameter(c1), new BasicParameter(ta, tb)) > 0);
		assertTrue(pc.compare(new BasicParameter(tb, tc), new InconsistencyParameter(c1)) < 0);
	}
}
