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
