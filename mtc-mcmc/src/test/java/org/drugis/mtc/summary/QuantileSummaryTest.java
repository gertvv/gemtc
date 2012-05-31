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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.drugis.mtc.test.ExampleResults;
import org.drugis.mtc.yadas.YadasResults;
import org.junit.Before;
import org.junit.Test;

public class QuantileSummaryTest {
	private static final double X_Q025 = 0.7401939;
	private static final double X_Q500 = 1.3545516;
	private static final double X_Q975 = 1.8711812;
	private static final double X_Q421 = 1.2772076;
	private static final double Y_Q025 = -0.85305988;
	private static final double Y_Q500 = 0.22301526;
	private static final double Y_Q975 = 1.17846957;
	private static final double Y_Q421 = 0.08862982;
	private static final double S_Q025 = 0.08489586;
	private static final double S_Q500 = 0.84826170;
	private static final double S_Q975 = 3.11779202;
	private static final double S_Q421 = 0.71246708;
	
	private ExampleResults d_results;
	private static final double EPSILON = 0.000001;
	
	@Before public void setUp() throws IOException {
		d_results = new ExampleResults();
	}
	
	@Test
	public void testGetDefaultProbabilities() {
		QuantileSummary x = new QuantileSummary(d_results, d_results.getParameters()[0]);
		assertEquals(0.025, x.getProbability(0), 0.0);
		assertEquals(0.5, x.getProbability(1), 0.0);
		assertEquals(0.975, x.getProbability(2), 0.0);
	}
	
	@Test
	public void testGetCustomProbabilities() {
		QuantileSummary x = new QuantileSummary(d_results, d_results.getParameters()[0], 
				new double[] { 0.18, 0.25 });
		assertEquals(0.18, x.getProbability(0), 0.0);
		assertEquals(0.25, x.getProbability(1), 0.0);
	}

	@Test
	public void testIndexOf() {
		QuantileSummary x = new QuantileSummary(d_results, d_results.getParameters()[0]);
		assertEquals(0, x.indexOf(0.025));
		assertEquals(1, x.indexOf(0.5));
		assertEquals(2, x.indexOf(0.975));
		assertEquals(-1, x.indexOf(0.12));
	}
	
	@Test
	public void testCalculations() {
		d_results.makeSamplesAvailable();
		
		QuantileSummary x = new QuantileSummary(d_results, d_results.getParameters()[0]);
		QuantileSummary y = new QuantileSummary(d_results, d_results.getParameters()[1]);
		QuantileSummary s = new QuantileSummary(d_results, d_results.getParameters()[2]);
		
		verifyDefaultResults(x, y, s);
	}
	
	@Test
	public void testCalculations2() {
		d_results.makeSamplesAvailable();
		
		double[] p = new double[] { 0.421 };
		QuantileSummary x = new QuantileSummary(d_results, d_results.getParameters()[0], p);
		QuantileSummary y = new QuantileSummary(d_results, d_results.getParameters()[1], p);
		QuantileSummary s = new QuantileSummary(d_results, d_results.getParameters()[2], p);
		
		verifyCustomResults(x, y, s);
	}

	@Test
	public void testDefined() {
		QuantileSummary x = new QuantileSummary(d_results, d_results.getParameters()[0]);
		assertFalse(x.getDefined());
		d_results.makeSamplesAvailable();
		assertTrue(x.getDefined());
	}
	
	@Test
	public void testWithUninitializedResults() {
		QuantileSummary x = new QuantileSummary(new YadasResults(), d_results.getParameters()[0]);
		assertFalse(x.getDefined());
	}
	
	@Test
	public void testResultsPreservedOnClear() {
		d_results.makeSamplesAvailable();
		QuantileSummary x = new QuantileSummary(d_results, d_results.getParameters()[0]);
		QuantileSummary y = new QuantileSummary(d_results, d_results.getParameters()[1]);
		QuantileSummary s = new QuantileSummary(d_results, d_results.getParameters()[2]);
		d_results.clear();

		verifyDefaultResults(x, y, s);
	}
	
	@Test
	public void testCalculationsOnAvailable() {
		QuantileSummary x = new QuantileSummary(d_results, d_results.getParameters()[0]);
		QuantileSummary y = new QuantileSummary(d_results, d_results.getParameters()[1]);
		QuantileSummary s = new QuantileSummary(d_results, d_results.getParameters()[2]);
		d_results.makeSamplesAvailable();
		
		verifyDefaultResults(x, y, s);
	}

	@Test
	public void testPropertyChangeOnAvailable() {
		QuantileSummary x = new QuantileSummary(d_results, d_results.getParameters()[0]);
		final List<String> properties = new ArrayList<String>();
		x.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				properties.add(evt.getPropertyName());
			}
		});
		d_results.makeSamplesAvailable();

		assertAllAndOnly(
				Collections.singletonList(QuantileSummary.PROPERTY_DEFINED),
				properties);
	}


	private void verifyDefaultResults(QuantileSummary x, QuantileSummary y, QuantileSummary s) {		
		// note that in R, vectors are indexed starting at 1.
		//		> lastHalf <- function(x) { c(x[251:500], x[751:1000]) }
		//		> quantile(lastHalf(data$x), c(0.025, 0.5, 0.975, 0.421), type=6)

		assertEquals(X_Q025, x.getQuantile(0), EPSILON);
		assertEquals(X_Q500, x.getQuantile(1), EPSILON);
		assertEquals(X_Q975, x.getQuantile(2), EPSILON);
		
		assertEquals(Y_Q025, y.getQuantile(0), EPSILON);
		assertEquals(Y_Q500, y.getQuantile(1), EPSILON);
		assertEquals(Y_Q975, y.getQuantile(2), EPSILON);
		
		assertEquals(S_Q025, s.getQuantile(0), EPSILON);
		assertEquals(S_Q500, s.getQuantile(1), EPSILON);
		assertEquals(S_Q975, s.getQuantile(2), EPSILON);
	}
	
	private void verifyCustomResults(QuantileSummary x, QuantileSummary y, QuantileSummary s) {
		assertEquals(X_Q421, x.getQuantile(0), EPSILON);
		assertEquals(Y_Q421, y.getQuantile(0), EPSILON);
		assertEquals(S_Q421, s.getQuantile(0), EPSILON);
	}
}
