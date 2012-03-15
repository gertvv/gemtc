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

package org.drugis.mtc.gui;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.drugis.common.event.TableModelEventMatcher;
import org.drugis.mtc.data.DataType;
import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.NetworkModel;
import org.junit.Before;
import org.junit.Test;

import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.list.ObservableList;
import com.jgoodies.binding.value.ValueHolder;

import static org.drugis.mtc.gui.MeasurementTableModel.*;

public class MeasurementTableModelTest {
	ObservableList<Treatment> d_treatments;
	ObservableList<Study> d_studies;
	ValueHolder d_measurementType;
	MeasurementTableModel d_model;
	private static double EPSILON = 0.00000001;

	@Before
	public void setUp() {
		Treatment fluox = new Treatment();
		fluox.setId("Fluox");
		Treatment parox = new Treatment();
		parox.setId("Parox");
		Treatment venla = new Treatment();
		venla.setId("Venla");
		d_treatments = new ArrayListModel<Treatment>(Arrays.asList(fluox, parox, venla));

		Study study1 = new Study();
		study1.setId("study1");
		StudyActions.addDefaultValueInserter(study1);
		study1.getMeasurements().add(new Measurement(fluox));
		study1.getMeasurements().add(new Measurement(venla));
		Study study2 = new Study();
		study1.setId("study2");
		StudyActions.addDefaultValueInserter(study2);
		d_studies = new ArrayListModel<Study>(Arrays.asList(study1, study2));

		d_measurementType = new ValueHolder(DataType.RATE);

		d_model = new MeasurementTableModel(d_studies, d_treatments, d_measurementType);
	}

	@Test
	public void testInitialColumns() {
		assertEquals(3, d_model.getColumnCount());

		assertEquals(COLNAME_ID, d_model.getColumnName(0));
		assertEquals(COLNAME_RESPONDERS, d_model.getColumnName(1));
		assertEquals(COLNAME_SAMPLESIZE, d_model.getColumnName(2));

		assertEquals(Object.class, d_model.getColumnClass(0));
		assertEquals(Integer.class, d_model.getColumnClass(1));
		assertEquals(Integer.class, d_model.getColumnClass(2));
	}

	@Test
	public void testNoneColumns() {
		d_measurementType.setValue(DataType.NONE);

		assertEquals(1, d_model.getColumnCount());
		assertEquals(COLNAME_ID, d_model.getColumnName(0));
		assertEquals(Object.class, d_model.getColumnClass(0));
	}

	@Test
	public void testContinuousColumns() {
		d_measurementType.setValue(DataType.CONTINUOUS);

		assertEquals(4, d_model.getColumnCount());

		assertEquals(COLNAME_ID, d_model.getColumnName(0));
		assertEquals(COLNAME_MEAN, d_model.getColumnName(1));
		assertEquals(COLNAME_STDDEV, d_model.getColumnName(2));
		assertEquals(COLNAME_SAMPLESIZE, d_model.getColumnName(3));

		assertEquals(Object.class, d_model.getColumnClass(0));
		assertEquals(Double.class, d_model.getColumnClass(1));
		assertEquals(Double.class, d_model.getColumnClass(2));
		assertEquals(Integer.class, d_model.getColumnClass(3));
	}

	@Test
	public void testMeasurementTypeEvents() {
		TableModelEvent event = new TableModelEvent(d_model, -1, -1, -1, TableModelEvent.UPDATE);
		TableModelListener mock = createStrictMock(TableModelListener.class);
		mock.tableChanged(TableModelEventMatcher.eqTableModelEvent(event));
		mock.tableChanged(TableModelEventMatcher.eqTableModelEvent(event));
		replay(mock);

		d_model.addTableModelListener(mock);
		d_measurementType.setValue(DataType.CONTINUOUS);
		d_measurementType.setValue(DataType.NONE);
		verify(mock);
	}

	@Test
	public void testInitialRows() {
		assertEquals(4, d_model.getRowCount());
	}

	@Test
	public void testInitialValues() {
		assertEquals(d_studies.get(0), d_model.getValueAt(0, 0));
		assertEquals(null, d_model.getValueAt(0, 1));
		assertEquals(null, d_model.getValueAt(0, 2));
		assertEquals(d_studies.get(1), d_model.getValueAt(3, 0));
		assertEquals(null, d_model.getValueAt(3, 1));
		assertEquals(null, d_model.getValueAt(3, 2));
		assertEquals(d_treatments.get(0), d_model.getValueAt(1, 0));
		assertEquals(0, d_model.getValueAt(1, 1));
		assertEquals(0, d_model.getValueAt(1, 2));
		assertEquals(d_treatments.get(2), d_model.getValueAt(2, 0));
		assertEquals(0, d_model.getValueAt(2, 1));
		assertEquals(0, d_model.getValueAt(2, 2));
	}

	@Test
	public void testAddStudyAtEndEventChaining() {
		TableModelEvent event = new TableModelEvent(d_model, 4, 4, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
		TableModelListener mock = createStrictMock(TableModelListener.class);
		d_model.addTableModelListener(mock);
		mock.tableChanged(TableModelEventMatcher.eqTableModelEvent(event));
		replay(mock);

		d_studies.add(new Study());
		verify(mock);
	}

	@Test
	public void testAddStudyMiddleEventChaining() {
		TableModelEvent event = new TableModelEvent(d_model, 3, 5, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
		TableModelListener mock = createStrictMock(TableModelListener.class);
		d_model.addTableModelListener(mock);
		mock.tableChanged(TableModelEventMatcher.eqTableModelEvent(event));
		replay(mock);

		Study study = new Study();
		study.getMeasurements().add(new Measurement(d_treatments.get(1)));
		study.getMeasurements().add(new Measurement(d_treatments.get(2)));
		d_studies.add(1, study);
		verify(mock);
	}

	@Test
	public void testAddTreatmentMiddleEventChaining() {
		TableModelEvent event = new TableModelEvent(d_model, 2, 2, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
		TableModelListener mock = createStrictMock(TableModelListener.class);
		d_model.addTableModelListener(mock);
		mock.tableChanged(TableModelEventMatcher.eqTableModelEvent(event));
		replay(mock);

		d_studies.get(0).getMeasurements().add(1, new Measurement(d_treatments.get(1)));
		verify(mock);

		assertEquals(d_treatments.get(0), d_model.getValueAt(1, 0));
		assertEquals(d_treatments.get(1), d_model.getValueAt(2, 0));
		assertEquals(d_treatments.get(2), d_model.getValueAt(3, 0));
		assertEquals(d_studies.get(1), d_model.getValueAt(4, 0));
	}

	@Test
	public void testRemoveTreatmentMiddleEventChaining() {
		TableModelEvent event = new TableModelEvent(d_model, 2, 2, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
		TableModelListener mock = createStrictMock(TableModelListener.class);
		d_model.addTableModelListener(mock);
		mock.tableChanged(TableModelEventMatcher.eqTableModelEvent(event));
		replay(mock);

		d_studies.get(0).getMeasurements().remove(1);
		verify(mock);

		assertEquals(d_studies.get(0), d_model.getValueAt(0, 0));
		assertEquals(d_treatments.get(0), d_model.getValueAt(1, 0));
		assertEquals(d_studies.get(1), d_model.getValueAt(2, 0));
	}

	@Test
	public void testRemoveStudyMiddleEventChaining() {
		Study study = new Study();
		study.getMeasurements().add(new Measurement(d_treatments.get(1)));
		study.getMeasurements().add(new Measurement(d_treatments.get(2)));
		d_studies.add(1, study);

		TableModelEvent event = new TableModelEvent(d_model, 3, 5, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
		TableModelListener mock = createStrictMock(TableModelListener.class);
		d_model.addTableModelListener(mock);
		mock.tableChanged(TableModelEventMatcher.eqTableModelEvent(event));
		replay(mock);

		d_studies.remove(1);
		verify(mock);
	}

	@Test
	public void testIsCellEditable() {
		assertFalse(d_model.isCellEditable(0, 0));
		assertFalse(d_model.isCellEditable(0, 1));
		assertFalse(d_model.isCellEditable(0, 2));
		assertFalse(d_model.isCellEditable(1, 0));
		assertTrue(d_model.isCellEditable(1, 1));
		assertTrue(d_model.isCellEditable(1, 2));
		assertFalse(d_model.isCellEditable(2, 0));
		assertTrue(d_model.isCellEditable(2, 1));
		assertTrue(d_model.isCellEditable(2, 2));
		assertFalse(d_model.isCellEditable(3, 0));
		assertFalse(d_model.isCellEditable(3, 1));
		assertFalse(d_model.isCellEditable(3, 2));
	}

	@Test
	public void testEditing() {
		d_model.setValueAt(37, 1, 1);
		assertEquals(37, (int)NetworkModel.findMeasurement(d_studies.get(0), d_treatments.get(0)).getResponders());
		assertEquals(37, d_model.getValueAt(1, 1));
		d_model.setValueAt(50, 1, 2);
		assertEquals(50, (int)NetworkModel.findMeasurement(d_studies.get(0), d_treatments.get(0)).getSampleSize());
		assertEquals(50, d_model.getValueAt(1, 2));

		d_measurementType.setValue(DataType.CONTINUOUS);
		assertEquals(50, d_model.getValueAt(1, 3));

		d_model.setValueAt(1.0, 1, 1);
		assertEquals(1.0, (Double)NetworkModel.findMeasurement(d_studies.get(0), d_treatments.get(0)).getMean(), EPSILON);
		assertEquals(1.0, (Double)d_model.getValueAt(1, 1), EPSILON);
		d_model.setValueAt(0.5, 1, 2);
		assertEquals(0.5, (Double)NetworkModel.findMeasurement(d_studies.get(0), d_treatments.get(0)).getStdDev(), EPSILON);
		assertEquals(0.5, (Double)d_model.getValueAt(1, 2), EPSILON);
		d_model.setValueAt(40, 1, 3);
		assertEquals(40, (int)NetworkModel.findMeasurement(d_studies.get(0), d_treatments.get(0)).getSampleSize());
		assertEquals(40, d_model.getValueAt(1, 3));
		
		d_measurementType.setValue(DataType.NONE);
		d_measurementType.setValue(DataType.RATE);
		assertEquals(37, d_model.getValueAt(1, 1));
		assertEquals(40, d_model.getValueAt(1, 2));
		d_measurementType.setValue(DataType.CONTINUOUS);
		assertEquals(1.0, (Double)d_model.getValueAt(1, 1), EPSILON);
		assertEquals(0.5, (Double)d_model.getValueAt(1, 2), EPSILON);
		assertEquals(40, d_model.getValueAt(1, 3));
	}
}


