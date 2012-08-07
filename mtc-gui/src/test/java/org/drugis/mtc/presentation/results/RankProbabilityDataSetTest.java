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

package org.drugis.mtc.presentation.results;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.drugis.mtc.Parameter;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.presentation.results.RankProbabilityDataset;
import org.drugis.mtc.summary.RankProbabilitySummary;
import org.drugis.mtc.test.FileResults;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.junit.Before;
import org.junit.Test;

public class RankProbabilityDataSetTest {
	private CategoryDataset d_dataSet;
	private Treatment d_ta;
	private Treatment d_tb;
	private Treatment d_tc;
	private RankProbabilitySummary d_summary;
	private FileResults d_results;

	@Before
	public void setUp() throws IOException {
		d_ta = new Treatment("A");
		d_tb = new Treatment("B");
		d_tc = new Treatment("C");
		d_results = new FileResults(
				RankProbabilityDataset.class.getResourceAsStream("rankProbabilitySamples.txt"),
				new Parameter[] { new BasicParameter(d_ta, d_tb), new BasicParameter(d_ta, d_tc) },
				1, 1000);
		d_summary = new RankProbabilitySummary(d_results, Arrays.asList(d_ta, d_tb, d_tc));
		d_dataSet = new RankProbabilityDataset(d_summary);
	}
	
	@Test
	public void testGetRowIndex() {
		Integer key = 3;
		assertEquals(key - 1, d_dataSet.getRowIndex("Rank " + key) );
	}
	
	@Test
	public void testGetColumnIndex() {
		assertEquals(0, d_dataSet.getColumnIndex(d_ta.getId()));
		assertEquals(1, d_dataSet.getColumnIndex(d_tb.getId()));
		assertEquals(2, d_dataSet.getColumnIndex(d_tc.getId()));
	}
	
	@Test
	public void testGetRowIndexThrows() {
		assertEquals(-1, d_dataSet.getRowIndex(10000));
	}
	
	@Test
	public void testGetColumnIndexThrows() {
		assertEquals(-1, d_dataSet.getColumnIndex(10000));
	}
	
	@Test
	public void testGetRowKey() {
		Integer index = 2;
		assertEquals("Rank " + (index+1), d_dataSet.getRowKey(index));
	}

	@Test
	public void testGetColumnKey() {
		assertEquals(d_tb.getId(), d_dataSet.getColumnKey(1));
	}
	
	@Test
	public void testGetRowKeys() {
		List<String> keys = new ArrayList<String>();
		for(int i = 0; i < d_summary.getTreatments().size(); ++i)
			keys.add("Rank " + (i+1));
		assertEquals(keys, d_dataSet.getRowKeys());
	}

	@Test
	public void testGetColumnKeys() {
		List<String> expected = Arrays.asList(new String[] { d_ta.getId(), d_tb.getId(), d_tc.getId() });
		assertEquals(expected, d_dataSet.getColumnKeys());
	}
	
	@Test
	public void testGetRowCount() {
		assertEquals (d_summary.getTreatments().size(), d_dataSet.getRowCount());
	}

	@Test
	public void testGetColumnCount() {
		assertEquals (d_summary.getTreatments().size(), d_dataSet.getColumnCount());
	}

	@Test
	public void testGetValue() {
		for (Treatment t : d_summary.getTreatments()) {
			for (int r = 1; r <= d_summary.getTreatments().size(); ++r) {
				assertEquals(0.0, d_dataSet.getValue(r - 1, d_summary.getTreatments().indexOf(t)));
				assertEquals(0.0, d_dataSet.getValue("Rank " + r, t)); 
			}
		}
		
		d_results.makeSamplesAvailable();
		for (Treatment t : d_summary.getTreatments()) {
			for (int r = 1; r <= d_summary.getTreatments().size(); ++r) {
				assertEquals(d_summary.getValue(t, r), d_dataSet.getValue(r - 1, d_summary.getTreatments().indexOf(t)));
				assertEquals(d_summary.getValue(t, r), d_dataSet.getValue("Rank " + r, t));
			}
		}
	}
	
	@Test
	public void testUpdateFiresEvent() {
		DatasetChangeListener mockListener = createMock(DatasetChangeListener.class);
		mockListener.datasetChanged((DatasetChangeEvent) anyObject());
		expectLastCall().atLeastOnce();
		replay(mockListener);
		
		d_dataSet.addChangeListener(mockListener);
		d_results.makeSamplesAvailable();
		verify(mockListener);
	}
}