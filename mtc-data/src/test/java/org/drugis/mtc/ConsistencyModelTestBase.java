package org.drugis.mtc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.drugis.common.threading.TaskUtil;
import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.junit.Before;
import org.junit.Test;

abstract public class ConsistencyModelTestBase {
	private static final double FACTOR = 0.05;
	
	private Mean d_mean = new Mean();
	private StandardDeviation d_stdDev = new StandardDeviation();

	private Treatment d_ta;

	private Treatment d_tb;

	private Treatment d_tc;

	private Study d_s1;

	private Study d_s2;

	private Study d_s3;

	private Network d_network;

	private ConsistencyModel d_model;

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
	}
	
	abstract protected ConsistencyModel createModel(Network network);
	
	@Test
	public void testIsNotReady() {
		assertFalse(d_model.isReady());
	}
	
	@Test
	public void testBasicParameters() throws InterruptedException {
		TaskUtil.run(d_model.getActivityTask());
		
		assertTrue(d_model.isReady());
		
		System.out.println(Arrays.toString(d_model.getResults().getParameters()));
		
		double[] dBA = ResultsUtil.getSamples(d_model.getResults(), d_model.getRelativeEffect(d_tb, d_ta), 0);
		double[] dBC = ResultsUtil.getSamples(d_model.getResults(), d_model.getRelativeEffect(d_tb, d_tc), 0);
		
		// Values below obtained via a run through regular JAGS with 30k/20k
		// iterations. Taking .05 sd as acceptable margin.
		double mAB = 0.4965705;
		double sAB = 0.4798996;
		assertEquals(-mAB, d_mean.evaluate(dBA), FACTOR * sAB);
		assertEquals(sAB, d_stdDev.evaluate(dBA), FACTOR * sAB);
		double mBC = -0.4095144;
		double sBC = 0.593866;
		assertEquals(mBC, d_mean.evaluate(dBC), FACTOR * sBC);
		assertEquals(sBC, d_stdDev.evaluate(dBC), FACTOR * sBC);
	}
	
	/*
	@Test def testIsNotReady() {
		model.isReady should be (false)
	}

	@Test def testBasicParameters() {
		run(model)
		model.isReady should be (true)

		val m = model
		val dAB = getSamples(model.getResults,
			m.getRelativeEffect(ta, tb), 0)
		val dBC = getSamples(model.getResults,
			m.getRelativeEffect(tb, tc), 0)

		// Values below obtained via a run through regular JAGS with 30k/20k
		// iterations. Taking .15 sd as acceptable margin (same as JAGS does
		// for testing against WinBUGS results).
		val mAB = 0.4965705
		val sAB = 0.4798996
		mean.evaluate(dAB) should be (mAB plusOrMinus f * sAB)
		stdDev.evaluate(dAB) should be(sAB plusOrMinus f * sAB)
		val mBC = -0.4095144
		val sBC = 0.593866
		mean.evaluate(dBC) should be (mBC plusOrMinus f * sBC)
		stdDev.evaluate(dBC) should be(sBC plusOrMinus f * sBC)
	}

	@Test def testDerivedParameters() {
		run(model)
		model.isReady should be (true)

		val m = model

		val dBA = getSamples(model.getResults,
			m.getRelativeEffect(tb, ta), 0)
		dBA should not be (null)
		val dAC = getSamples(model.getResults,
			m.getRelativeEffect(ta, tc), 0)
		val mAC = 0.08705606
		val sAC = 0.5046929
		mean.evaluate(dAC) should be (mAC plusOrMinus f * sAC)
		stdDev.evaluate(dAC) should be(sAC plusOrMinus f * sAC)
	}
	
	def run(model: MCMCModel) {
		val th = ThreadHandler.getInstance()
		val task = model.getActivityTask()
		th.scheduleTask(task)
		waitUntilReady(task)
	}*/
}