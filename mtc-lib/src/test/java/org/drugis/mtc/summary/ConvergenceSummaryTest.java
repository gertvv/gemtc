package org.drugis.mtc.summary;

import static org.drugis.common.JUnitUtil.assertAllAndOnly;
import static org.junit.Assert.assertEquals;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.drugis.mtc.BasicParameter;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.Treatment;
import org.drugis.mtc.convergence.GelmanRubinAcceptanceTest;
import org.drugis.mtc.convergence.GelmanRubinConvergence;
import org.drugis.mtc.summary.Summary;
import org.drugis.mtc.util.FileResults;
import org.drugis.mtc.yadas.RandomEffectsVariance;
import org.junit.Before;
import org.junit.Test;

import scala.actors.threadpool.Arrays;

public class ConvergenceSummaryTest {
	private Parameter[] d_parameters;
	private FileResults d_results;

	@Before
	public void setUp() throws IOException {
		InputStream is = GelmanRubinAcceptanceTest.class.getResourceAsStream("conv-samples.txt");
		Treatment t1 = new Treatment("iPCI");
		Treatment t2 = new Treatment("mPCI");
		Treatment t3 = new Treatment("sPCI");
		d_parameters = new Parameter[] {
				new BasicParameter(t1, t2), new BasicParameter(t2, t3), new RandomEffectsVariance()	
		};
		d_results = new FileResults(is, d_parameters, 3, 10000);
	}
	
	@Test
	public void testCreation() {
		ConvergenceSummary cs = new ConvergenceSummary(d_results, d_parameters[0]);
		assertEquals(false, cs.getDefined());
	}
	
	@Test
	public void testCalculations() {
		d_results.makeSamplesAvailable();
		ConvergenceSummary cs = new ConvergenceSummary(d_results, d_parameters[0]);
		assertEquals(true, cs.getDefined());
		assertEquals(GelmanRubinConvergence.diagnose(d_results, d_parameters[0]), cs.getScaleReduction(), 0.0);
	}
	
	
	@Test
	public void testResultsPreservedOnClear() {
		d_results.makeSamplesAvailable();
		ConvergenceSummary cs = new ConvergenceSummary(d_results, d_parameters[0]);
		double convergence = GelmanRubinConvergence.diagnose(d_results, d_parameters[0]);
		d_results.clear();
		assertEquals(true, cs.getDefined());
		assertEquals(convergence, cs.getScaleReduction(), 0.0);
	}
	
	@Test
	public void testResultsCalculatedOnAvailable() {
		ConvergenceSummary cs = new ConvergenceSummary(d_results, d_parameters[0]);
		d_results.makeSamplesAvailable();
		double convergence = GelmanRubinConvergence.diagnose(d_results, d_parameters[0]);
		assertEquals(convergence, cs.getScaleReduction(), 0.0);
	}
	
	@Test
	public void testPropertyChangeOnAvailable() {
		ConvergenceSummary cs = new ConvergenceSummary(d_results, d_parameters[0]);
		final List<String> properties = new ArrayList<String>();
		cs.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				properties.add(evt.getPropertyName());
			}
		});
		d_results.makeSamplesAvailable();

		assertAllAndOnly(
				Arrays.asList(new String[] {
						Summary.PROPERTY_DEFINED, 
						ConvergenceSummary.PROPERTY_PSRF }),
				properties);
	}
}
