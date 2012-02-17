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
		
		d_model = new YadasConsistencyModel(d_network);
	}

	@Test 
	public void testSimulationIterations() {
		assertEquals(100000, d_model.getSimulationIterations());
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
		assertEquals(20000, d_model.getBurnInIterations());
		d_model.setBurnInIterations(10000);
		assertEquals(10000, d_model.getBurnInIterations());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testBurnInIterationsMultipleOfReportingInterval() {
		d_model.setBurnInIterations(10001);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testBurnInIterationsPositive() {
		d_model.setBurnInIterations(0);
	}
}