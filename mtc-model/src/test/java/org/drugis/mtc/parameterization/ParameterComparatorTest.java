/*
 * This file is part of the GeMTC software for MTC model generation and
 * analysis. GeMTC is distributed from http://drugis.org/gemtc.
 * Copyright (C) 2009-2012 Gert van Valkenhoef.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.drugis.mtc.parameterization;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.drugis.mtc.model.Treatment;
import org.junit.Test;

public class ParameterComparatorTest {
	@Test
	public void testCompareBasicParameters() {
		ParameterComparator pc = new ParameterComparator();
		
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
		ParameterComparator pc = new ParameterComparator();
		
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
	public void testCompareSplitParameters() {
		ParameterComparator pc = new ParameterComparator();
		
		Treatment ta = new Treatment("A");
		Treatment tb = new Treatment("B");
		
		assertTrue(pc.compare(new SplitParameter(ta, tb, true), new SplitParameter(ta, tb, false)) < 0);
		assertTrue(pc.compare(new SplitParameter(ta, tb, true), new SplitParameter(ta, tb, true)) == 0);
	}
	
	@Test
	public void testCompareMixed() {
		ParameterComparator pc = new ParameterComparator();
		
		Treatment ta = new Treatment("A");
		Treatment tb = new Treatment("B");
		Treatment tc = new Treatment("C");
		
		List<Treatment> c1 = Arrays.asList(ta, tb, tc, ta);
		
		assertTrue(pc.compare(new BasicParameter(ta, tb), new InconsistencyParameter(c1)) < 0);
		assertTrue(pc.compare(new InconsistencyParameter(c1), new BasicParameter(ta, tb)) > 0);
		assertTrue(pc.compare(new BasicParameter(tb, tc), new InconsistencyParameter(c1)) < 0);
		
		assertTrue(pc.compare(new BasicParameter(ta, tb), new SplitParameter(ta, tb, true)) < 0);
		assertTrue(pc.compare(new SplitParameter(ta, tb, true), new BasicParameter(ta, tb)) > 0);
		assertTrue(pc.compare(new BasicParameter(tb, tc), new SplitParameter(ta, tb, true)) < 0);
		
		// Not testing SplitParameter / InconsistencyParameter comparisons because they should not occur together.
	}
}
