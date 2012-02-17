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
	public void testResults() throws InterruptedException {
		TaskUtil.run(d_model.getActivityTask());
		
		assertTrue(d_model.isReady());
		
		double[] dBA = ResultsUtil.getSamples(d_model.getResults(), d_model.getRelativeEffect(d_tb, d_ta), 3);
		double[] dBC = ResultsUtil.getSamples(d_model.getResults(), d_model.getRelativeEffect(d_tb, d_tc), 3);
		
		// Values below obtained via a run through regular JAGS with 30k/20k
		// iterations. Taking .05 sd as acceptable margin.
		double mBC = -0.4095144;
		double sBC = 0.593866;
		assertEquals(mBC, d_mean.evaluate(dBC), FACTOR * sBC);
		assertEquals(sBC, d_stdDev.evaluate(dBC), FACTOR * sBC);
		double mAB = 0.4965705;
		double sAB = 0.4798996;
		assertEquals(-mAB, d_mean.evaluate(dBA), FACTOR * sAB);
		assertEquals(sAB, d_stdDev.evaluate(dBA), FACTOR * sAB);
		
		// Test values of derived parameters
		double mAC = 0.08705606;
		double sAC = 0.5046929;
		double[] dAC = ResultsUtil.getSamples(d_model.getResults(), d_model.getRelativeEffect(d_ta, d_tc), 3);
		assertEquals(mAC, d_mean.evaluate(dAC), FACTOR * sAC);
		assertEquals(sAC, d_stdDev.evaluate(dAC), FACTOR * sAC);
		double[] dAB = ResultsUtil.getSamples(d_model.getResults(), d_model.getRelativeEffect(d_ta, d_tb), 3);
		assertEquals(mAB, d_mean.evaluate(dAB), FACTOR * sAB);
		assertEquals(sAB, d_stdDev.evaluate(dAB), FACTOR * sAB);
	}
}