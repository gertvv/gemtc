/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
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

package org.drugis.mtc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class DichotomousNetworkBuilderTest {
	private Study<DichotomousMeasurement> study(String id,
			DichotomousMeasurement[] m) {
		return Study$.MODULE$.buildDichotomous(id, m);
	}

	private DichotomousNetworkBuilder d_builder;
	private Treatment d_ta = new Treatment("A");
	private Treatment d_tb = new Treatment("B");
	private Treatment d_tc = new Treatment("C");
	private DichotomousMeasurement d_s1a = new DichotomousMeasurement(d_ta, 5, 100);
	private DichotomousMeasurement d_s1b = new DichotomousMeasurement(d_tb, 23, 100);
	private Study<DichotomousMeasurement> d_s1 = study("1", new DichotomousMeasurement[]{d_s1a, d_s1b});
	private DichotomousMeasurement d_s2b = new DichotomousMeasurement(d_tb, 12, 43);
	private DichotomousMeasurement d_s2c = new DichotomousMeasurement(d_tc, 15, 40);
	private Study<DichotomousMeasurement> d_s2 = study("2", new DichotomousMeasurement[]{d_s2b, d_s2c});

	@Before public void setUp() {
		d_builder = new DichotomousNetworkBuilder();
	}

	@Test public void testEmptyBuild() {
		@SuppressWarnings("unchecked")
		Network<DichotomousMeasurement> n = d_builder.buildNetwork();

		assertNotNull(n);
		assertTrue(n.treatments().isEmpty());
		assertTrue(n.studies().isEmpty());
	}

	@Test public void testBuild() {
		d_builder.add("1", "A", d_s1a.responders(), d_s1a.sampleSize());
		d_builder.add("1", "B", d_s1b.responders(), d_s1b.sampleSize());
		d_builder.add("2", "B", d_s2b.responders(), d_s2b.sampleSize());
		d_builder.add("2", "C", d_s2c.responders(), d_s2c.sampleSize());
		@SuppressWarnings("unchecked")
		Network<DichotomousMeasurement> n = d_builder.buildNetwork();

		assertNotNull(n);
		assertEquals(3, n.treatments().size());
		assertTrue(n.treatments().contains(d_ta));
		assertTrue(n.treatments().contains(d_tb));
		assertTrue(n.treatments().contains(d_tc));
		assertEquals(2, n.studies().size());
		assertTrue(n.studies().contains(d_s1));
		assertTrue(n.studies().contains(d_s2));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testDuplicateEntry() {
		d_builder.add("1", "A", d_s1a.responders(), d_s1a.sampleSize());
		d_builder.add("1", "A", d_s1b.responders(), d_s1b.sampleSize());
	}
}
