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
import java.util.Arrays;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.drugis.mtc.Parameter;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.presentation.results.RankProbabilityDataset;
import org.drugis.mtc.presentation.results.RankProbabilityTableModel;
import org.drugis.mtc.summary.RankProbabilitySummary;
import org.drugis.mtc.test.FileResults;
import org.junit.Before;
import org.junit.Test;

public class RankProbabilityTableModelTest {
	private Treatment d_ta;
	private Treatment d_tb;
	private Treatment d_tc;
	private RankProbabilitySummary d_summary;
	private FileResults d_results;
	private RankProbabilityTableModel d_model;

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
		d_model = new RankProbabilityTableModel(d_summary);
	}
	
	@Test
	public void testColumnCount() {
		assertEquals(4, d_model.getColumnCount());
	}
	
	@Test
	public void testRowCount() {
		assertEquals(3, d_model.getRowCount());
	}
	
	@Test
	public void testRowName() {
		assertEquals(d_ta.getId(), d_model.getValueAt(0, 0));
		assertEquals(d_tb.getId(), d_model.getValueAt(1, 0));
		assertEquals(d_tc.getId(), d_model.getValueAt(2, 0));
	}
	
	@Test
	public void testColumnName() {
		assertEquals("Drug", d_model.getColumnName(0));
		assertEquals("Rank 1", d_model.getColumnName(1));
		assertEquals("Rank 2", d_model.getColumnName(2));
		assertEquals("Rank 3", d_model.getColumnName(3));
	}
	
	@Test
	public void testColumnClass() {
		assertEquals(String.class, d_model.getColumnClass(0));
		assertEquals(Double.class, d_model.getColumnClass(1));
		assertEquals(Double.class, d_model.getColumnClass(2));
		assertEquals(Double.class, d_model.getColumnClass(3));
	}
	
	@Test
	public void testValueAt() {
		for (int t = 0; t < d_summary.getTreatments().size(); ++t) {
			for (int r = 1; r <= d_summary.getTreatments().size(); ++r) {
				assertEquals("", d_model.getValueAt(t, r));
			}
		}
		
		d_results.makeSamplesAvailable();
		for (int t = 0; t < d_summary.getTreatments().size(); ++t) {
			for (int r = 1; r <= d_summary.getTreatments().size(); ++r) {
				assertEquals(d_summary.getValue(d_summary.getTreatments().get(t), r), d_model.getValueAt(t, r));
			}
		}
	}
	
	@Test
	public void testUpdateFires() {
		TableModelListener mockListener = createMock(TableModelListener.class);
		mockListener.tableChanged((TableModelEvent) anyObject());
		expectLastCall().atLeastOnce();
		replay(mockListener);
				
		d_model.addTableModelListener(mockListener);
		d_results.makeSamplesAvailable();
		verify(mockListener);
	}
}
