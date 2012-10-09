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
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.util.ResultsUtil;
import org.junit.Before;
import org.junit.Test;

public class YadasNodeSplitIT {
	public static final double FACTOR = 0.1;

	private Mean d_mean = new Mean();
	private StandardDeviation d_stdDev = new StandardDeviation();

	private Network d_network;
	private Treatment d_mpci;
	private Treatment d_spci;

	@Before
	public void setUp() throws JAXBException {
		InputStream is = ContinuousDataIT.class.getResourceAsStream("vlaar-longterm.xml");
		d_network = JAXBHandler.readNetwork(is);

		d_mpci = new Treatment("mPCI");
		d_spci = new Treatment("sPCI");
	}

	@Test
	public void testResult() throws InterruptedException {
		YadasNodeSplitModel model = new YadasNodeSplitModel(d_network, new BasicParameter(d_mpci, d_spci), new YadasModelFactory().getDefaults());
		model.setExtendSimulation(ExtendSimulation.FINISH);
		model.setSimulationIterations(100000);
		TaskUtil.run(model.getActivityTask());

		assertTrue(model.isReady());

		double[] direct = ResultsUtil.getSamples(model.getResults(), model.getResults().findParameter(model.getDirectEffect()), 0);
		double[] indirect = ResultsUtil.getSamples(model.getResults(), model.getResults().findParameter(model.getIndirectEffect()), 0);

		double mDir = -1.01660;
		double sDir = 0.3287;
		double mInd = -1.16773;
		double sInd = 0.4338;

		assertEquals(mDir, d_mean.evaluate(direct), FACTOR * sDir);
		assertEquals(sDir, d_stdDev.evaluate(direct), FACTOR * sDir);
		assertEquals(mInd, d_mean.evaluate(indirect), FACTOR * sInd);
		assertEquals(sInd, d_stdDev.evaluate(indirect), FACTOR * sInd);
	}
}