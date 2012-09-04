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

package org.drugis.mtc.yadas;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.junit.Before;
import org.junit.Test;

public class YadasConsistencyModelTest {
	private Treatment d_ta;
	private Treatment d_tb;
	private Study d_s1;
	private Network d_network;
	private YadasConsistencyModel d_model;

	@Before
	public void setUp() {
		d_ta = new Treatment("A");
		d_tb = new Treatment("B");
		d_s1 = new Study("1");
		d_s1.getMeasurements().addAll(Arrays.asList(
				new Measurement(d_ta, 1, 100), new Measurement(d_tb, 1, 100)));
		d_network = new Network();
		d_network.getTreatments().addAll(Arrays.asList(d_ta, d_tb));
		d_network.getStudies().add(d_s1);

		d_model = new YadasConsistencyModel(d_network, new YadasModelFactory().getDefaults());
	}

	@Test
	public void testSimulationIterations() {
		assertEquals(50000, d_model.getSimulationIterations());
		d_model.setSimulationIterations(10000);
		assertEquals(10000, d_model.getSimulationIterations());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSimulationIterationsMultipleOfReportingInterval() {
		d_model.setSimulationIterations(10001);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSimulationIterationsPositive() {
		d_model.setSimulationIterations(0);
	}

	@Test
	public void testBurnInIterations() {
		assertEquals(20000, d_model.getSettings().getTuningIterations());
		d_model.setTuningIterations(10000);
		assertEquals(10000, d_model.getSettings().getTuningIterations());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testBurnInIterationsMultipleOfReportingInterval() {
		d_model.setTuningIterations(10001);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testBurnInIterationsPositive() {
		d_model.setTuningIterations(0);
	}
}