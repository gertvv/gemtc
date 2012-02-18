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
