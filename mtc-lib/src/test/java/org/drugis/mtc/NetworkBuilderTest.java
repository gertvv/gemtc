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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.StringUtils;
import org.drugis.mtc.util.ScalaUtil;
import org.junit.Before;
import org.junit.Test;

public class NetworkBuilderTest {
	public static class NoneBuilder<TreatmentType> extends NetworkBuilder<NoneMeasurement, TreatmentType> {
		public NoneBuilder() {
			super();
		}
		
		public NoneBuilder(Transformer<TreatmentType, String> idToString) {
			super(idToString);
		}
		
		public void add(String studyId, TreatmentType treatmentId) {
			Treatment t = makeTreatment(treatmentId);
			add(studyId, t, new NoneMeasurement(t));
		}
	}
	
	private Study<NoneMeasurement> study(String id, NoneMeasurement[] m) {
		return new Study<NoneMeasurement>(id, ScalaUtil.toScalaMap(measurementMap(m)));
	}

	private Map<Treatment, NoneMeasurement> measurementMap(NoneMeasurement[] ms) {
		Map<Treatment, NoneMeasurement> map = new HashMap<Treatment, NoneMeasurement>();
		for (NoneMeasurement m : ms) {
			map.put(m.treatment(), m);
		}
		return map;
	}

	private NoneBuilder<String> d_builder;
	private Treatment d_ta = new Treatment("A");
	private Treatment d_tb = new Treatment("B");
	private Treatment d_tc = new Treatment("C");
	private Study<NoneMeasurement> d_s1 = study("1", new NoneMeasurement[]{new NoneMeasurement(d_ta), new NoneMeasurement(d_tb)});
	private Study<NoneMeasurement> d_s2 = study("2", new NoneMeasurement[]{new NoneMeasurement(d_tb), new NoneMeasurement(d_tc)});

	@Before 
	public void setUp() {
		d_builder = new NoneBuilder<String>();
	}

	@Test 
	public void testEmptyBuild() {
		Network<NoneMeasurement> n = d_builder.buildNetwork();

		assertNotNull(n);
		assertTrue(n.treatments().isEmpty());
		assertTrue(n.studies().isEmpty());
	}

	@Test 
	public void testBuild() {
		d_builder.add("1", "A");
		d_builder.add("1", "B");
		d_builder.add("2", "B");
		d_builder.add("2", "C");
		Network<NoneMeasurement> n = d_builder.buildNetwork();

		assertNotNull(n);
		assertEquals(3, n.treatments().size());
		assertTrue(n.treatments().contains(d_ta));
		assertTrue(n.treatments().contains(d_tb));
		assertTrue(n.treatments().contains(d_tc));
		assertEquals(2, n.studies().size());
		assertTrue(n.studies().contains(d_s1));
		assertTrue(n.studies().contains(d_s2));
		
		HashMap<String, Treatment> expected = new HashMap<String, Treatment>();
		expected.put("A", d_ta);
		expected.put("B", d_tb);
		expected.put("C", d_tc);
		assertEquals(expected, d_builder.getTreatmentMap());
	}
	
	@Test
	public void testIdTransform() {
		NoneBuilder<Integer> builder = new NoneBuilder<Integer>(new Transformer<Integer, String>() {
			public String transform(Integer input) {
				return StringUtils.repeat("A", input.intValue());
			}
		});
		
		builder.add("1", 5);
		HashMap<Integer, Treatment> expected = new HashMap<Integer, Treatment>();
		expected.put(5, new Treatment("AAAAA"));
		assertEquals(expected, builder.getTreatmentMap());
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testTreatmentMapUnmodifiable() {
		d_builder.getTreatmentMap().put("A", d_ta);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testDuplicateEntry() {
		d_builder.add("1", "A");
		d_builder.add("1", "A");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalTreatmentId() {
		d_builder.add("1", "-A"); // allowed chars: A-Za-z0-9_
	}
}
