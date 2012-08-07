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

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.drugis.common.JUnitUtil;
import org.drugis.common.threading.Task;
import org.drugis.common.threading.TaskUtil;
import org.drugis.common.threading.status.TaskTerminatedModel;
import org.drugis.mtc.DefaultModelFactory;
import org.drugis.mtc.DichotomousNetworkBuilder;
import org.drugis.mtc.InconsistencyModel;
import org.drugis.mtc.MCMCModel.ExtendSimulation;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.parameterization.InconsistencyParameter;
import org.drugis.mtc.presentation.SimulationInconsistencyWrapper;
import org.drugis.mtc.summary.QuantileSummary;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class NetworkInconsistencyTableModelTest {
	private NetworkInconsistencyFactorsTableModel d_tableModel;
	private SimulationInconsistencyWrapper<String> d_inconsistencyWrapper;
	private Task d_modelConstructionPhase;
	
	@Before
	public void setUp() {
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
		
		final InconsistencyModel model = DefaultModelFactory.instance().getInconsistencyModel(network);
		d_inconsistencyWrapper = new SimulationInconsistencyWrapper<String>(model, builder.getTreatmentMap());
		d_inconsistencyWrapper.getModel().setTuningIterations(100);
		d_inconsistencyWrapper.getModel().setSimulationIterations(100);
		d_inconsistencyWrapper.getModel().setExtendSimulation(ExtendSimulation.FINISH);
		d_modelConstructionPhase = d_inconsistencyWrapper.getModel().getActivityTask().getModel().getStartState();
		d_tableModel = new NetworkInconsistencyFactorsTableModel(d_inconsistencyWrapper, 
				new TaskTerminatedModel(d_modelConstructionPhase));
	}
	
	@Test
	public void testGetColumnCount() {
		assertEquals(2, d_tableModel.getColumnCount());
	}

	@Test
	public void testGetRowCount() throws InterruptedException {
		assertEquals(0, d_tableModel.getRowCount());
		TaskUtil.run(d_modelConstructionPhase);
		assertEquals(d_inconsistencyWrapper.getInconsistencyFactors().size(), d_tableModel.getRowCount());
	}

	@Test
	public void testValueAt() throws InterruptedException {
		TaskUtil.run(d_inconsistencyWrapper.getModel().getActivityTask());
		assertEquals("Fluoxetine, Paroxetine, Sertraline", d_tableModel.getValueAt(0, 0));
		
		InconsistencyParameter ip = (InconsistencyParameter)d_inconsistencyWrapper.getInconsistencyFactors().get(0);
		QuantileSummary summary = d_inconsistencyWrapper.getQuantileSummary(ip);
		Object valueAt = d_tableModel.getValueAt(0, 1);
		assertEquals(summary, valueAt);
	}
	
	@Test
	public void testValueNA() {
		assertEquals("N/A", d_tableModel.getValueAt(0, 1));
	}
	
	@Test
	public void testUpdateFiresTableDataChangedEvent() throws InterruptedException {
		// Expect any number of table changed events
		TableModelListener mock = createMock(TableModelListener.class);
		mock.tableChanged((TableModelEvent)JUnitUtil.eqEventObject(new TableModelEvent(d_tableModel)));
		EasyMock.expectLastCall().anyTimes();
		replay(mock);
		
		// Run the model
		d_tableModel.addTableModelListener(mock);
		TaskUtil.run(d_inconsistencyWrapper.getModel().getActivityTask());		
		EasyMock.verify(mock);
	}
}