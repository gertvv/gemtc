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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.drugis.common.JUnitUtil;
import org.drugis.common.threading.TaskUtil;
import org.drugis.mtc.ConsistencyModel;
import org.drugis.mtc.DefaultModelFactory;
import org.drugis.mtc.DichotomousNetworkBuilder;
import org.drugis.mtc.MCMCModel.ExtendSimulation;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.presentation.ConsistencyWrapper;
import org.drugis.mtc.presentation.SimulationConsistencyWrapper;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class NetworkTableModelTest {
	private NetworkRelativeEffectTableModel<String> d_tableModel;
	private List<String> d_alternatives;
	private SimulationConsistencyWrapper<String> d_consistencyWrapper;

	@Before
	public void setUp() {
		d_alternatives = Arrays.asList("Fluoxetine", "Paroxetine", "Sertraline");
		DichotomousNetworkBuilder<String> builder = new DichotomousNetworkBuilder<String>();
		builder.add("Bennie", "Fluoxetine", 1, 100);
		builder.add("Bennie", "Sertraline", 1, 100);
		builder.add("Chouinard", "Fluoxetine", 1, 100);
		builder.add("Chouinard", "Paroxetine", 1, 100);
		builder.add("De Wilde", "Paroxetine", 1, 100);
		builder.add("De Wilde", "Fluoxetine", 1, 100);
		builder.add("Fava", "Sertraline", 1, 100);
		builder.add("Fava", "Fluoxetine", 1, 100);
		builder.add("Fava", "Paroxetine", 1, 100);
		Network network = builder.buildNetwork();
		
		final ConsistencyModel model = DefaultModelFactory.instance().getConsistencyModel(network);
		d_consistencyWrapper = new SimulationConsistencyWrapper<String>(model, d_alternatives, builder.getTreatmentMap());
		d_tableModel = new NetworkRelativeEffectTableModel<String>(d_alternatives, d_consistencyWrapper);
	}
	
	@Test
	public void testGetColumnCount() {
		assertEquals(d_alternatives.size(), d_tableModel.getColumnCount());
	}

	@Test
	public void testGetRowCount() {
		assertEquals(d_alternatives.size(), d_tableModel.getRowCount());
	}

	@Test
	public void testValueAt() {
		assertTrue(d_tableModel.getColumnCount() > 0);
		assertTrue(d_tableModel.getRowCount() > 0);

		assertEquals(null, d_tableModel.getDescriptionAt(0, 0));
		assertEquals(null, d_tableModel.getDescriptionAt(1, 1));
		assertEquals(null, d_tableModel.getDescriptionAt(2, 2));
		assertEquals(d_alternatives.get(0), d_tableModel.getValueAt(0, 0));
		assertEquals(d_alternatives.get(1), d_tableModel.getValueAt(1, 1));
		assertEquals(d_alternatives.get(2), d_tableModel.getValueAt(2, 2));

		
		ConsistencyWrapper<String> consModel = d_consistencyWrapper;
		Parameter relativeEffect01 = consModel.getRelativeEffect(d_alternatives.get(0), d_alternatives.get(1));
		Parameter relativeEffect10 = consModel.getRelativeEffect(d_alternatives.get(1), d_alternatives.get(0));
		Parameter relativeEffect20 = consModel.getRelativeEffect(d_alternatives.get(2), d_alternatives.get(0));

		assertSame(consModel.getQuantileSummary(relativeEffect01), d_tableModel.getValueAt(0, 1));
		assertEquals("\"Paroxetine\" relative to \"Fluoxetine\"", d_tableModel.getDescriptionAt(0, 1));
		assertSame(consModel.getQuantileSummary(relativeEffect10), d_tableModel.getValueAt(1, 0));
		assertEquals("\"Fluoxetine\" relative to \"Paroxetine\"", d_tableModel.getDescriptionAt(1, 0));
		assertSame(consModel.getQuantileSummary(relativeEffect20), d_tableModel.getValueAt(2, 0));
		assertEquals("\"Fluoxetine\" relative to \"Sertraline\"", d_tableModel.getDescriptionAt(2, 0));
	}
	
	
	@Test
	public void testUpdateFiresTableDataChangedEvent() throws InterruptedException {
		// Set up very short run of consistencyModel
		ConsistencyWrapper<String> model =  d_consistencyWrapper;
		model.getModel().setTuningIterations(100);
		model.getModel().setSimulationIterations(100);
		model.getModel().setExtendSimulation(ExtendSimulation.FINISH);

		// Expect any number of table changed events
		TableModelListener mock = createMock(TableModelListener.class);
		mock.tableChanged((TableModelEvent)JUnitUtil.eqEventObject(new TableModelEvent(d_tableModel)));
		EasyMock.expectLastCall().anyTimes();
		replay(mock);
		
		// Run the model
		d_tableModel.addTableModelListener(mock);
		TaskUtil.run(model.getModel().getActivityTask());		
		EasyMock.verify(mock);
	}
}
