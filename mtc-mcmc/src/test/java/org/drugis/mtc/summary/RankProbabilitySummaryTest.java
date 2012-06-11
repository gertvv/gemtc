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
import java.util.Arrays;
import java.util.List;

import org.drugis.mtc.Parameter;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.test.FileResults;
import org.drugis.mtc.yadas.YadasResults;
import org.junit.Before;
import org.junit.Test;

public class RankProbabilitySummaryTest {
	private Treatment d_ta;
	private Treatment d_tb;
	private Treatment d_tc;
	private RankProbabilitySummary d_summary;
	private FileResults d_results;
	private List<Treatment> d_treatments;
	
	@Before
	public void setUp() throws IOException {
		d_ta = new Treatment("A");
		d_tb = new Treatment("B");
		d_tc = new Treatment("C");
		d_results = new FileResults(
				RankProbabilitySummaryTest.class.getResourceAsStream("rankProbabilitySamples.txt"),
				new Parameter[] { new BasicParameter(d_ta, d_tb), new BasicParameter(d_ta, d_tc) },
				1, 1000);
		d_treatments = Arrays.asList(new Treatment[] { d_ta, d_tb, d_tc });
		d_summary = new RankProbabilitySummary(d_results, d_treatments);
	}
	
	@Test
	public void testResults() {
//	              A     B     C    correct rank
//	       [1,] 0.287 0.149 0.564  3
//	       [2,] 0.557 0.206 0.237  2
//	       [3,] 0.156 0.645 0.199  1
//		Generated in R using:
//			> AA <- rep(0, times=1000)
//			> AB <- rnorm(1000, 0.2, 0.3)
//			> AC <- rnorm(1000, -0.1, 0.32)
//			> l <- mapply(function(a, b, c) { c(a, b, c) }, AA, AB, AC)
//			> ranks <- apply(l, 2, rank)
//			> rankprob <- sapply(c(1, 2, 3), function(opt) { sapply(c(1, 2, 3), function(rank) { sum(ranks[opt, ] == rank)/dim(ranks)[2] }) })
		d_results.makeSamplesAvailable();
		assertEquals(0.268, d_summary.getValue(d_ta, 3), 0.001);
		assertEquals(0.566, d_summary.getValue(d_ta, 2), 0.001);
		assertEquals(0.166, d_summary.getValue(d_ta, 1), 0.001);
		assertEquals(0.150, d_summary.getValue(d_tb, 3), 0.001);
		assertEquals(0.204, d_summary.getValue(d_tb, 2), 0.001);
		assertEquals(0.646, d_summary.getValue(d_tb, 1), 0.001);
		assertEquals(0.582, d_summary.getValue(d_tc, 3), 0.001);
		assertEquals(0.230, d_summary.getValue(d_tc, 2), 0.001);
		assertEquals(0.188, d_summary.getValue(d_tc, 1), 0.001);
	}
	
	@Test
	public void testDefined() {
		Summary x = d_summary;
		assertFalse(x.getDefined());
		d_results.makeSamplesAvailable();
		assertTrue(x.getDefined());
	}
	
	@Test
	public void testWithUninitializedResults() {
		RankProbabilitySummary x = new RankProbabilitySummary(new YadasResults(), d_treatments);
		assertFalse(x.getDefined());
	}
	
	@Test
	public void testPropertyChangeOnAvailable() {
		final List<String> properties = new ArrayList<String>();
		d_summary.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				properties.add(evt.getPropertyName());
			}
		});
		d_results.makeSamplesAvailable();

		assertAllAndOnly(
				Arrays.asList(new String[] {
						Summary.PROPERTY_DEFINED, 
						RankProbabilitySummary.PROPERTY_VALUE}),
				properties);
	}
}
