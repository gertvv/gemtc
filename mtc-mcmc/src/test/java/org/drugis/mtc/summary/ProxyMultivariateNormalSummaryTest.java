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

package org.drugis.mtc.summary;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import org.drugis.common.JUnitUtil;
import org.drugis.mtc.test.ExampleResults;
import org.junit.Before;
import org.junit.Test;

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
