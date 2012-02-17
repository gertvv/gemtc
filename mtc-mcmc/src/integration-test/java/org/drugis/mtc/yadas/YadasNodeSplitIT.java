package org.drugis.mtc.yadas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.drugis.common.threading.TaskUtil;
import org.drugis.mtc.ResultsUtil;
import org.drugis.mtc.model.JAXBHandler;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;
import org.junit.Before;
import org.junit.Test;

public class YadasNodeSplitIT {
	public static final double FACTOR = 0.1;

	private Mean d_mean = new Mean();
	private StandardDeviation d_stdDev = new StandardDeviation();
	
	private Network d_network;
//	private Treatment d_ipci;
	private Treatment d_mpci;
	private Treatment d_spci;

	@Before
	public void setUp() throws JAXBException {
		InputStream is = ContinuousDataIT.class.getResourceAsStream("vlaar-longterm.xml");
		d_network = JAXBHandler.readNetwork(is);

//		d_ipci = new Treatment("iPCI");
		d_mpci = new Treatment("mPCI");
		d_spci = new Treatment("sPCI");
	}

	@Test
	public void testResult() throws InterruptedException {
		YadasNodeSplitModel model = new YadasNodeSplitModel(d_network, new BasicParameter(d_mpci, d_spci));
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