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
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.drugis.common.threading.TaskUtil;
import org.drugis.mtc.MCMCModel.ExtendSimulation;
import org.drugis.mtc.model.JAXBHandler;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.util.ResultsUtil;
import org.junit.Before;
import org.junit.Test;

public class ContinuousDataIT {
	private static final double FACTOR = 0.05;
	private double d_m;
	private double d_s;
	private Mean d_mean;
	private StandardDeviation d_stdDev;
	private Network d_network;
	private Treatment d_psych;
	private Treatment d_usual;

	@Before
	public void setUp() throws JAXBException {
		// data from Welton et. al., Am J Epidemiol 2009;169:1158-1165
		d_m = -1.362791; // mean(d)
		d_s = 0.982033; // sd(d)
		d_mean = new Mean();
		d_stdDev = new StandardDeviation();


		InputStream is = ContinuousDataIT.class.getResourceAsStream("weltonBP.xml");
		d_network = JAXBHandler.readNetwork(is);

		d_psych = new Treatment("psych");
		d_usual = new Treatment("usual");
	}

	@Test
	public void testResult() throws InterruptedException {
		YadasConsistencyModel model = new YadasConsistencyModel(d_network, new YadasModelFactory().getDefaults());
		model.setExtendSimulation(ExtendSimulation.FINISH);
		model.setSimulationIterations(100000);
		TaskUtil.run(model.getActivityTask());

		assertTrue(model.isReady());
		int d = model.getResults().findParameter(model.getRelativeEffect(d_psych, d_usual));
		assertEquals(-d_m, d_mean.evaluate(ResultsUtil.getSamples(model.getResults(), d, 0)), FACTOR * d_s);
		assertEquals(d_s, d_stdDev.evaluate(ResultsUtil.getSamples(model.getResults(), d, 0)), FACTOR * d_s);
	}
}
