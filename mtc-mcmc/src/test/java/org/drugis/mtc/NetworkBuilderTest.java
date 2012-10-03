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

package org.drugis.mtc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.StringUtils;
import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.NetworkBuilder;
import org.drugis.mtc.model.NoneNetworkBuilder;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.junit.Before;
import org.junit.Test;

public class NetworkBuilderTest {
	
	private static final class EmptyStringTransformer<TreatmentType> implements Transformer<TreatmentType, String> {
		public String transform(TreatmentType input) {
			return "";
		}
	}
	
	private Study study(String id, Measurement[] m) {
		final Study study = new Study(id);
		study.getMeasurements().addAll(Arrays.asList(m));
		return study;
	}

	private NoneNetworkBuilder<String> d_builder;
	private Treatment d_ta = new Treatment("A");
	private Treatment d_tb = new Treatment("B");
	private Treatment d_tc = new Treatment("C");
	private Study d_s1 = study("1", new Measurement[]{new Measurement(d_ta), new Measurement(d_tb)});
	private Study d_s2 = study("2", new Measurement[]{new Measurement(d_tb), new Measurement(d_tc)});

	@Before 
	public void setUp() {
		d_builder = new NoneNetworkBuilder<String>(new NetworkBuilder.ToStringTransformer<String>(), new EmptyStringTransformer<String>());
	}

	@Test 
	public void testEmptyBuild() {
		Network n = d_builder.buildNetwork();

		assertNotNull(n);
		assertTrue(n.getTreatments().isEmpty());
		assertTrue(n.getStudies().isEmpty());
	}

	@Test 
	public void testBuild() {
		d_builder.add("1", "A");
		d_builder.add("1", "B");
		d_builder.add("2", "B");
		d_builder.add("2", "C");
		Network n = d_builder.buildNetwork();

		assertNotNull(n);
		assertEquals(3, n.getTreatments().size());
		assertTrue(n.getTreatments().contains(d_ta));
		assertTrue(n.getTreatments().contains(d_tb));
		assertTrue(n.getTreatments().contains(d_tc));
		assertEquals(2, n.getStudies().size());
		assertTrue(n.getStudies().contains(d_s1));
		assertTrue(n.getStudies().contains(d_s2));
		
		HashMap<String, Treatment> expected = new HashMap<String, Treatment>();
		expected.put("A", d_ta);
		expected.put("B", d_tb);
		expected.put("C", d_tc);
		assertEquals(expected, d_builder.getTreatmentMap());
	}
	
	@Test
	public void testIdTransform() {
		NoneNetworkBuilder<Integer> builder = new NoneNetworkBuilder<Integer>(new Transformer<Integer, String>() {
			public String transform(Integer input) {
				return StringUtils.repeat("A", input.intValue());
			}
		}, new EmptyStringTransformer<Integer>());
		
		builder.add("1", 5);
		HashMap<Integer, Treatment> expected = new HashMap<Integer, Treatment>();
		expected.put(5, new Treatment("AAAAA"));
		assertEquals(expected, builder.getTreatmentMap());
	}
	
	@Test
	public void testDescriptionTransform() {
		NoneNetworkBuilder<Integer> builder = new NoneNetworkBuilder<Integer>(new NetworkBuilder.ToStringTransformer<Integer>(),
				new Transformer<Integer, String>() {
					public String transform(Integer input) {
						return StringUtils.repeat("A", input.intValue());
					}
				});
		
		builder.add("1", 5);
		HashMap<Integer, Treatment> expected = new HashMap<Integer, Treatment>();
		expected.put(5, new Treatment("5", "AAAAA"));
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
