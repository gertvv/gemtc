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

import static org.drugis.common.JUnitUtil.assertAllAndOnly;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.drugis.mtc.test.ExampleResults;
import org.junit.Before;
import org.junit.Test;

public class TransformedMultivariateNormalSummaryTest {
	// calculation in R: half <- data[c(251:500,751:1000),]
	// transform, going from baseline 0 to baseline 1 : half <- data.frame(x1=-half$x, x2=-half$x+half$y, x3=-half$x + half$s);
	private static final double[] MEANS = new double[] { -1.3401944, -1.1456006, -0.3155935 };
	private static final double[][] COV = new double[][] {
		{ 0.08459550, 0.08175367, 0.09404607 },
		{ 0.08175367, 0.35161866, 0.12930651 },
		{ 0.09404607, 0.12930651, 0.66335719 }
	};
	private static final double[][] TRANSFORM = new double[][] {
		{-1, 0, 0},
		{-1, 1, 0},
		{-1, 0, 1},
	};
	private static final double EPSILON = 0.000001;

	private ExampleResults d_results;
	private MCMCMultivariateNormalSummary d_nested;

	@Before
	public void setUp() throws IOException {
		d_results = new ExampleResults();
		d_nested = new MCMCMultivariateNormalSummary(d_results, d_results.getParameters());
	}

	@Test
	public void testDefined() {
		TransformedMultivariateNormalSummary summary = new TransformedMultivariateNormalSummary(d_nested , TRANSFORM);
		assertFalse(summary.getDefined());
		d_results.makeSamplesAvailable();
		assertTrue(summary.getDefined());
	}
	
	@Test
	public void testCalculateAfterConstruction() {
		d_results.makeSamplesAvailable();
		TransformedMultivariateNormalSummary summary = new TransformedMultivariateNormalSummary(d_nested , TRANSFORM);
		verifyResults(summary);
	}

	@Test
	public void testCalculateAfterResultsBecomeAvailable() {
		TransformedMultivariateNormalSummary summary = new TransformedMultivariateNormalSummary(d_nested , TRANSFORM);
		d_results.makeSamplesAvailable();
		verifyResults(summary);
	}
	
	private static void verifyResults(TransformedMultivariateNormalSummary summary) {
		assertArrayEquals(MEANS, summary.getMeanVector(), EPSILON);
		assertArrayEquals(COV[0], summary.getCovarianceMatrix()[0], EPSILON);
		assertArrayEquals(COV[1], summary.getCovarianceMatrix()[1], EPSILON);
		assertArrayEquals(COV[2], summary.getCovarianceMatrix()[2], EPSILON);
	}
	
	
	@Test
	public void testPropertyChangeOnAvailable() {
		TransformedMultivariateNormalSummary x = new TransformedMultivariateNormalSummary(d_nested, TRANSFORM);
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
	
}
