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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.drugis.common.threading.TaskUtil;
import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.InconsistencyParameter;
import org.drugis.mtc.util.ResultsUtil;
import org.junit.Before;
import org.junit.Test;

abstract public class InconsistencyModelTestBase {
	private static final double FACTOR = 0.10;
	
	private Mean d_mean = new Mean();
	private StandardDeviation d_stdDev = new StandardDeviation();

	private Treatment d_ta;
	private Treatment d_tb;
	private Treatment d_tc;
	private Study d_s1;
	private Study d_s2;
	private Study d_s3;
	private Network d_network;
	private InconsistencyModel d_model;
	
	@Before
	public void setUp() {
		d_ta = new Treatment("A");
		d_tb = new Treatment("B");
		d_tc = new Treatment("C");
		d_s1 = new Study("1");
		d_s1.getMeasurements().addAll(Arrays.asList(
				new Measurement(d_ta, 9, 140),
				new Measurement(d_tb, 23, 140),
				new Measurement(d_tc, 10, 138)));
		d_s2 = new Study("2");
		d_s2.getMeasurements().addAll(Arrays.asList(
				new Measurement(d_ta, 79, 702),
				new Measurement(d_tb, 77, 694)));
		d_s3 = new Study("3");
		d_s3.getMeasurements().addAll(Arrays.asList(
				new Measurement(d_ta, 18, 671),
				new Measurement(d_tc, 21, 535)));
		d_network = new Network();
		d_network.getTreatments().addAll(Arrays.asList(d_ta, d_tb, d_tc));
		d_network.getStudies().addAll(Arrays.asList(d_s1, d_s2, d_s3));
		d_model = createModel(d_network);
		d_model.setSimulationIterations(100000);

	}

	abstract protected InconsistencyModel createModel(Network network);

	@Test
	public void testIsNotReady() {
		assertFalse(d_model.isReady());
	}
	
	@Test
	public void testResults() throws InterruptedException {
		TaskUtil.run(d_model.getActivityTask());
		
		assertTrue(d_model.isReady());
		
		final InconsistencyParameter w = new InconsistencyParameter(Arrays.asList(d_ta, d_tb, d_tc, d_ta));
		assertEquals(Collections.singletonList(w), d_model.getInconsistencyFactors());

		double[] dAB = ResultsUtil.getSamples(d_model.getResults(), d_model.getRelativeEffect(d_ta, d_tb), 3);
		double[] dBC = ResultsUtil.getSamples(d_model.getResults(), d_model.getRelativeEffect(d_tb, d_tc), 3);
		double[] wABC = ResultsUtil.getSamples(d_model.getResults(), w, 3);

		// Values below obtained via a run through regular JAGS with 30k/20k
		// iterations. Taking .15 sd as acceptable margin (same as JAGS does
		// for testing against WinBUGS results).
		double mAB = 0.4713884;
		double sAB = 0.4838365;
		assertEquals(mAB, d_mean.evaluate(dAB), FACTOR * sAB);
		assertEquals(sAB, d_stdDev.evaluate(dAB), FACTOR * sAB);
		double mBC = -0.4645146;
		double sBC = 0.6111192;
		assertEquals(mBC, d_mean.evaluate(dBC), FACTOR * sBC);
		assertEquals(sBC, d_stdDev.evaluate(dBC), FACTOR * sBC);
		double mABC = -0.1466253;
		double sABC = 0.4568596;
		assertEquals(mABC, d_mean.evaluate(wABC), FACTOR * sABC);
		assertEquals(sABC, d_stdDev.evaluate(wABC), FACTOR * sABC);

		double[] dBA = ResultsUtil.getSamples(d_model.getResults(), d_model.getRelativeEffect(d_tb, d_ta), 3);
		assertEquals(-mAB, d_mean.evaluate(dBA), FACTOR * sAB);
		assertEquals(sAB, d_stdDev.evaluate(dBA), FACTOR * sAB);
		
		double[] dAC = ResultsUtil.getSamples(d_model.getResults(), d_model.getRelativeEffect(d_ta, d_tc), 3);
		double mAC = 0.1534991;
		double sAC = 0.5514409;
		assertEquals(mAC, d_mean.evaluate(dAC), FACTOR * sAC);
		assertEquals(sAC, d_stdDev.evaluate(dAC), FACTOR * sAC);
	}
}