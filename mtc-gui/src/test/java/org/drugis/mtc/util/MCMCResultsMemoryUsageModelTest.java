/*
 * This file is part of ADDIS (Aggregate Data Drug Information System).
 * ADDIS is distributed from http://drugis.org/.
 * Copyright © 2009 Gert van Valkenhoef, Tommi Tervonen.
 * Copyright © 2010 Gert van Valkenhoef, Tommi Tervonen, Tijs Zwinkels,
 * Maarten Jacobs, Hanno Koeslag, Florin Schimbinschi, Ahmad Kamal, Daniel
 * Reid.
 * Copyright © 2011 Gert van Valkenhoef, Ahmad Kamal, Daniel Reid, Florin
 * Schimbinschi.
 * Copyright © 2012 Gert van Valkenhoef, Daniel Reid, Joël Kuiper, Wouter
 * Reckman.
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

package org.drugis.mtc.util;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;

import org.drugis.common.JUnitUtil;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.parameterization.RandomEffectsVariance;
import org.drugis.mtc.test.FileResults;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class MCMCResultsMemoryUsageModelTest {
	private static final int AVAILABLE_SAMPLES = 10000;
	private Parameter[] d_parameters;
	private FileResults d_results;


	@Before
	public void setUp() throws IOException {
		Treatment t1 = new Treatment("iPCI");
		Treatment t2 = new Treatment("mPCI");
		Treatment t3 = new Treatment("sPCI");
		d_parameters = new Parameter[] {
				new BasicParameter(t1, t2), new BasicParameter(t2, t3), new RandomEffectsVariance()
		};
		d_results = readSamples();
	}

	@Test
	public void sanityCheck() {
		assertEquals(64, Double.SIZE);
	}

	@Test
	public void testInitialValueEmpty() {
		MCMCResultsMemoryUsageModel model = new MCMCResultsMemoryUsageModel(d_results);
		assertEquals("0.0 KB", model.getValue());
	}

	@Test
	public void testInitialValueFull() {
		d_results.makeSamplesAvailable();
		MCMCResultsMemoryUsageModel model = new MCMCResultsMemoryUsageModel(d_results);

		assertEquals("0.7 MB", model.getValue());
	}

	@Test
	public void testInitialValueOther() {
		FakeResults results = new FakeResults(3, 100, 3);
		MCMCResultsMemoryUsageModel model = new MCMCResultsMemoryUsageModel(results);
		assertEquals("7.2 KB", model.getValue());
	}

	@Test
	public void testInitialValueLarge() {
		MCMCResults results = new FakeResults(3, 100 * 1000, 3);
		MCMCResultsMemoryUsageModel model = new MCMCResultsMemoryUsageModel(results);
		assertEquals("7.2 MB", model.getValue());
	}

	@Test
	public void testInitialValueVeryLarge() {
		MCMCResults results = new FakeResults(3, 1000 * 1000 * 10, 3);
		MCMCResultsMemoryUsageModel model = new MCMCResultsMemoryUsageModel(results);
		assertEquals("720.0 MB", model.getValue());
	}

	@Test
	public void testInitialValueTiny() {
		MCMCResults results = new FakeResults(3, 1, 3);
		MCMCResultsMemoryUsageModel model = new MCMCResultsMemoryUsageModel(results);
		assertEquals("0.1 KB", model.getValue());
	}

	@Test
	public void testValueUpdate() {
		MCMCResultsMemoryUsageModel model = new MCMCResultsMemoryUsageModel(d_results);
		PropertyChangeListener listener = JUnitUtil.mockStrictListener(model, "value", null, "0.7 MB");
		model.addValueChangeListener(listener);

		d_results.makeSamplesAvailable();
		assertEquals("0.7 MB", model.getValue());
		EasyMock.verify(listener);
	}

	private FileResults readSamples() throws IOException {
		InputStream is = EmpiricalDensityDatasetTest.class.getResourceAsStream("conv-samples.txt");
		FileResults results = new FileResults(is, d_parameters, 3, AVAILABLE_SAMPLES);
		is.close();
		return results;
	}
}
