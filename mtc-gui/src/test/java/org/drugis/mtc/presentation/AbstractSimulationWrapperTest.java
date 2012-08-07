/*
 * This file is part of ADDIS (Aggregate Data Drug Information System).
 * ADDIS is distributed from http://drugis.org/.
 * Copyright (C) 2009 Gert van Valkenhoef, Tommi Tervonen.
 * Copyright (C) 2010 Gert van Valkenhoef, Tommi Tervonen, 
 * Tijs Zwinkels, Maarten Jacobs, Hanno Koeslag, Florin Schimbinschi, 
 * Ahmad Kamal, Daniel Reid.
 * Copyright (C) 2011 Gert van Valkenhoef, Ahmad Kamal, 
 * Daniel Reid, Florin Schimbinschi.
 * Copyright (C) 2012 Gert van Valkenhoef, Daniel Reid, 
 * Joël Kuiper, Wouter Reckman.
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

package org.drugis.mtc.presentation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.drugis.mtc.ConsistencyModel;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.model.Treatment;
import org.junit.Before;
import org.junit.Test;

public class AbstractSimulationWrapperTest {
	private AbstractMTCSimulationWrapper<String, ConsistencyModel> d_model;
	private List<String> d_treatments;

	@Before
	public void setUp() {
		d_treatments = Arrays.asList("A", "B", "C");
		BidiMap<String, Treatment> treatmentMap = new DualHashBidiMap<String, Treatment>();
		ArrayList<Treatment> treatmentList = new ArrayList<Treatment>();
		for(String s : d_treatments) { 
			final Treatment t = new Treatment(s, "");
			treatmentMap.put(s, t);
			treatmentList.add(t);
		}
		ConsistencyModel mtc = MockConsistencyModel.buildMockSimulationConsistencyModel(treatmentList);
		d_model = new AbstractMTCSimulationWrapper<String, ConsistencyModel>(mtc, "Stub Model", treatmentMap) {};
	}

	@Test
	public void testGetQuantileSummary() {
		Parameter dAB = d_model.getRelativeEffect(d_treatments.get(0), d_treatments.get(1));
		Parameter dAC = d_model.getRelativeEffect(d_treatments.get(0), d_treatments.get(2));
		assertNotNull(d_model.getQuantileSummary(dAB));
		assertSame(d_model.getQuantileSummary(dAB), d_model.getQuantileSummary(dAB));
		assertNotNull(d_model.getQuantileSummary(dAC));
		assertNotSame(d_model.getQuantileSummary(dAB), d_model.getQuantileSummary(dAC));
	}
}
