package org.drugis.mtc.summary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.drugis.common.JUnitUtil.assertAllAndOnly;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import scala.actors.threadpool.Arrays;

public class NormalSummaryTest {
	private static final double Y_STDEV = 0.5222134;
	private static final double Y_MEAN = 0.1945938;
	private static final double X_STDEV = 0.2908531;
	private static final double X_MEAN = 1.340194;
	private ExampleResults d_results;
	private static final double EPSILON = 0.000001;
	
	@Before public void setUp() throws IOException {
		d_results = new ExampleResults();
	}
	
	@Test
	public void testCalculations() {
		d_results.makeSamplesAvailable();
		
		NormalSummary x = new NormalSummary(d_results, d_results.getParameters()[0]);
		NormalSummary y = new NormalSummary(d_results, d_results.getParameters()[1]);
		
		verifyResults(x, y);
	}
	
	@Test
	public void testDefined() {
		Summary x = new NormalSummary(d_results, d_results.getParameters()[0]);
		assertFalse(x.getDefined());
		d_results.makeSamplesAvailable();
		assertTrue(x.getDefined());
	}
	
	@Test
	public void testResultsPreservedOnClear() {
		d_results.makeSamplesAvailable();
		NormalSummary x = new NormalSummary(d_results, d_results.getParameters()[0]);
		NormalSummary y = new NormalSummary(d_results, d_results.getParameters()[1]);
		d_results.clear();

		verifyResults(x, y);
	}

	
	@Test
	public void testResultsCalculatedOnAvailable() {
		NormalSummary x = new NormalSummary(d_results, d_results.getParameters()[0]);
		NormalSummary y = new NormalSummary(d_results, d_results.getParameters()[1]);
		d_results.makeSamplesAvailable();
		
		verifyResults(x, y);
	}

	@Test
	public void testPropertyChangeOnAvailable() {
		NormalSummary x = new NormalSummary(d_results, d_results.getParameters()[0]);
		final List<String> properties = new ArrayList<String>();
		x.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				properties.add(evt.getPropertyName());
			}
		});
		d_results.makeSamplesAvailable();

		assertAllAndOnly(
				Arrays.asList(new String[] {
						Summary.PROPERTY_DEFINED, 
						NormalSummary.PROPERTY_MEAN, 
						NormalSummary.PROPERTY_STANDARD_DEVIATION}),
				properties);
	}


	private void verifyResults(NormalSummary x, NormalSummary y) {		
		// calculation in R: mean(c(x[251:500], x[751:1000])): last half of each chain.
		// note that in R, vectors are indexed starting at 1.

		assertEquals(X_MEAN, x.getMean(), EPSILON);
		assertEquals(X_STDEV, x.getStandardDeviation(), EPSILON);

		assertEquals(Y_MEAN, y.getMean(), EPSILON);
		assertEquals(Y_STDEV, y.getStandardDeviation(), EPSILON);
	}
}
