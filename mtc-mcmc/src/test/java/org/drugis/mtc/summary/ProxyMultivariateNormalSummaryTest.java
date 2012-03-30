package org.drugis.mtc.summary;

import static org.junit.Assert.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import org.drugis.common.JUnitUtil;
import org.drugis.mtc.test.ExampleResults;
import org.junit.Before;
import org.junit.Test;
import static org.easymock.EasyMock.*;

public class ProxyMultivariateNormalSummaryTest {

	private static final double EPSILON = 0.000000001;
	private ProxyMultivariateNormalSummary d_summary;

	@Before
	public void setUp() {
		d_summary = new ProxyMultivariateNormalSummary();
	}
	
	@Test
	public void testNullInitialization() {
		assertFalse(d_summary.getDefined());
	}

	@Test
	public void testSetNested() {
		d_summary.setNested(new SimpleMultivariateNormalSummary(new double[]{0, 0}, new double[][]{ {1, 1}, {0, 1} }));
		assertTrue(d_summary.getDefined());
		assertArrayEquals(new double[]{0, 0}, d_summary.getMeanVector(), EPSILON);
		assertArrayEquals(new double[]{1, 1}, d_summary.getCovarianceMatrix()[0], EPSILON);
		assertArrayEquals(new double[]{0, 1}, d_summary.getCovarianceMatrix()[1], EPSILON);
	}
	
	@Test
	public void testEventPropagation() throws IOException {
		ExampleResults results = new ExampleResults();
		MultivariateNormalSummary summary = new MCMCMultivariateNormalSummary(results, results.getParameters());
		d_summary.setNested(summary);
		
		PropertyChangeListener listener = createStrictMock(PropertyChangeListener.class);
		listener.propertyChange(JUnitUtil.eqPropertyChangeEvent(
				new PropertyChangeEvent(d_summary, Summary.PROPERTY_DEFINED, false, true)));
		listener.propertyChange(JUnitUtil.eqPropertyChangeEventIgnoreValues(
				new PropertyChangeEvent(d_summary, MultivariateNormalSummary.PROPERTY_MEAN_VECTOR, null, null)));
		listener.propertyChange(JUnitUtil.eqPropertyChangeEventIgnoreValues(
				new PropertyChangeEvent(d_summary, MultivariateNormalSummary.PROPERTY_COVARIANCE_MATRIX, null, null)));
		replay(listener);
		
		d_summary.addPropertyChangeListener(listener);
		
		results.makeSamplesAvailable();
		
		verify(listener);
	}
	
	@Test
	public void testRemoveListener() throws IOException {
		ExampleResults results = new ExampleResults();
		MultivariateNormalSummary summary = new MCMCMultivariateNormalSummary(results, results.getParameters());
		d_summary.setNested(summary);
		d_summary.setNested(new SimpleMultivariateNormalSummary(new double[]{0}, new double[][]{ {1} }));

		PropertyChangeListener listener = createStrictMock(PropertyChangeListener.class);
		replay(listener);
		d_summary.addPropertyChangeListener(listener);
		results.makeSamplesAvailable();
		verify(listener);
	}
	
	@Test
	public void testSetNestedEvents() throws IOException {
		ExampleResults results = new ExampleResults();
		results.makeSamplesAvailable();
		MultivariateNormalSummary summary = new MCMCMultivariateNormalSummary(results, results.getParameters());

		PropertyChangeListener listener = createStrictMock(PropertyChangeListener.class);
		listener.propertyChange(JUnitUtil.eqPropertyChangeEvent(
				new PropertyChangeEvent(d_summary, Summary.PROPERTY_DEFINED, false, true)));
		listener.propertyChange(JUnitUtil.eqPropertyChangeEventIgnoreValues(
				new PropertyChangeEvent(d_summary, MultivariateNormalSummary.PROPERTY_MEAN_VECTOR, null, null)));
		listener.propertyChange(JUnitUtil.eqPropertyChangeEventIgnoreValues(
				new PropertyChangeEvent(d_summary, MultivariateNormalSummary.PROPERTY_COVARIANCE_MATRIX, null, null)));
		replay(listener);
		
		d_summary.addPropertyChangeListener(listener);
		d_summary.setNested(summary);
		verify(listener);
	}
	
	@Test
	public void testSetNestedEventsUndefined() throws IOException {
		ExampleResults results = new ExampleResults();
		MultivariateNormalSummary summary = new MCMCMultivariateNormalSummary(results, results.getParameters());

		PropertyChangeListener listener = createStrictMock(PropertyChangeListener.class);
		replay(listener);
		
		d_summary.addPropertyChangeListener(listener);
		d_summary.setNested(summary);
		d_summary.setNested(null);
		verify(listener);
	}
	
	@Test
	public void testSetNestedEventsUndefined2() throws IOException {
		ExampleResults results = new ExampleResults();
		MultivariateNormalSummary summary = new MCMCMultivariateNormalSummary(results, results.getParameters());
		d_summary.setNested(new SimpleMultivariateNormalSummary(new double[]{0}, new double[][]{ {1} }));

		PropertyChangeListener listener = createStrictMock(PropertyChangeListener.class);
		listener.propertyChange(JUnitUtil.eqPropertyChangeEvent(
				new PropertyChangeEvent(d_summary, Summary.PROPERTY_DEFINED, true, false)));
		listener.propertyChange(JUnitUtil.eqPropertyChangeEvent(
				new PropertyChangeEvent(d_summary, MultivariateNormalSummary.PROPERTY_MEAN_VECTOR, null, null)));
		listener.propertyChange(JUnitUtil.eqPropertyChangeEvent(
				new PropertyChangeEvent(d_summary, MultivariateNormalSummary.PROPERTY_COVARIANCE_MATRIX, null, null)));
		replay(listener);
		
		d_summary.addPropertyChangeListener(listener);
		d_summary.setNested(summary);
		verify(listener);
	}
}
