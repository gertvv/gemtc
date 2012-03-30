package org.drugis.mtc.summary;

import static org.drugis.common.JUnitUtil.assertAllAndOnly;
import static org.junit.Assert.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.drugis.mtc.Parameter;
import org.drugis.mtc.test.ExampleResults;
import org.junit.Before;
import org.junit.Test;

public class MCMCMultivariateNormalSummaryTest {
	// calculation in R: half <- data[c(251:500,751:1000),]; mean(half); cov(half): last half of each chain.
	// note that in R, vectors are indexed starting at 1.
	private static final double[] MEANS = new double[] { 1.3401944, 0.1945938, 1.0246009 };
	private static final double[][] COV = new double[][] {
		{  0.084595497, 0.002841824, -0.009450577 },
		{  0.002841824, 0.272706810,  0.038102256 },
		{ -0.009450577, 0.038102256,  0.559860536 }
	};
	private static final double EPSILON = 0.000001;

	private ExampleResults d_results;

	@Before
	public void setUp() throws IOException {
		d_results = new ExampleResults();
	}
	
	@Test
	public void testDefined() {
		Summary x = new MCMCMultivariateNormalSummary(d_results, d_results.getParameters());
		assertFalse(x.getDefined());
		d_results.makeSamplesAvailable();
		assertTrue(x.getDefined());
	}
	
	@Test
	public void testCalculationsAfterEvent() {
		MultivariateNormalSummary summary = new MCMCMultivariateNormalSummary(d_results, d_results.getParameters());
		d_results.makeSamplesAvailable();
		verifyResults(summary);
	}

	@Test
	public void testCalculationsOnConstruction() {
		d_results.makeSamplesAvailable();
		MultivariateNormalSummary summary = new MCMCMultivariateNormalSummary(d_results, d_results.getParameters());
		verifyResults(summary);
	}
	
	@Test
	public void testCalculationsReorderedParameters() {
		Parameter[] parameters = new Parameter[] { d_results.getParameters()[1], d_results.getParameters()[0] };
		MultivariateNormalSummary summary = new MCMCMultivariateNormalSummary(d_results, parameters);
		d_results.makeSamplesAvailable();
		
		assertArrayEquals(new double[] { MEANS[1], MEANS[0] }, summary.getMeanVector(), EPSILON);
		assertArrayEquals(new double[] { COV[1][1], COV[1][0] }, summary.getCovarianceMatrix()[0], EPSILON);
		assertArrayEquals(new double[] { COV[0][1], COV[0][0] }, summary.getCovarianceMatrix()[1], EPSILON);
	}
	
	@Test
	public void testResultsPreservedOnClear() {
		MultivariateNormalSummary summary = new MCMCMultivariateNormalSummary(d_results, d_results.getParameters());
		d_results.makeSamplesAvailable();
		d_results.clear();
		verifyResults(summary);
	}
	
	@Test
	public void testPropertyChangeOnAvailable() {
		MCMCMultivariateNormalSummary x = new MCMCMultivariateNormalSummary(d_results, d_results.getParameters());
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
						MultivariateNormalSummary.PROPERTY_MEAN_VECTOR, 
						MultivariateNormalSummary.PROPERTY_COVARIANCE_MATRIX}),
				properties);
	}

	private void verifyResults(MultivariateNormalSummary summary) {
		assertArrayEquals(MEANS, summary.getMeanVector(), EPSILON);
		assertArrayEquals(COV[0], summary.getCovarianceMatrix()[0], EPSILON);
		assertArrayEquals(COV[1], summary.getCovarianceMatrix()[1], EPSILON);
		assertArrayEquals(COV[2], summary.getCovarianceMatrix()[2], EPSILON);
	}
}
