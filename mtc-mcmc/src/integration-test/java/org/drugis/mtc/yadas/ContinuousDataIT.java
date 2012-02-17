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
		YadasConsistencyModel model = new YadasConsistencyModel(d_network);
		TaskUtil.run(model.getActivityTask());
		
		assertTrue(model.isReady());
		int d = model.getResults().findParameter(model.getRelativeEffect(d_psych, d_usual));
		assertEquals(-d_m, d_mean.evaluate(ResultsUtil.getSamples(model.getResults(), d, 0)), FACTOR * d_s);
		assertEquals(d_s, d_stdDev.evaluate(ResultsUtil.getSamples(model.getResults(), d, 0)), FACTOR * d_s);
	}
}
