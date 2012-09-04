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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;

import org.drugis.common.beans.ListPropertyChangeProxy;
import org.drugis.mtc.data.DataType;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.NetworkModel;

import com.jgoodies.binding.list.ObservableList;
import com.jgoodies.binding.value.ValueModel;

public class MeasurementTableModel extends AbstractTableModel {
	public static final String COLNAME_STDDEV = "Standard deviation";
	public static final String COLNAME_MEAN = "Mean";
	public static final String COLNAME_SAMPLESIZE = "Sample size";
	public static final String COLNAME_RESPONDERS = "Responders";
	public static final String COLNAME_ID = "Id";

	private static final long serialVersionUID = -1186064425875064988L;

	private ValueModel d_measurementType;
	private ObservableList<Study> d_studies;
	private List<Integer> d_studyIndex = new ArrayList<Integer>();
	@SuppressWarnings("unused") private ListPropertyChangeProxy<Study> d_studyIdProxy;
	@SuppressWarnings("unused") private ListPropertyChangeProxy<Treatment> d_treatmentIdProxy;

	private ListDataListener d_measurementListener = new ListDataListener() {
		public void contentsChanged(ListDataEvent e) {
			throw new UnsupportedOperationException();
		}
		public void intervalAdded(ListDataEvent e) {
			treatmentsAdded(findStudy(e.getSource()), e.getIndex0(), e.getIndex1());
		}
		public void intervalRemoved(ListDataEvent e) {
			treatmentsRemoved(findStudy(e.getSource()), e.getIndex0(), e.getIndex1());
		}
	};

	public MeasurementTableModel(
			ObservableList<Study> studies,
			ObservableList<Treatment> treatments,
			ValueModel measurementType) {
		d_studies = studies;
		d_measurementType = measurementType;
		d_measurementType.addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				fireTableStructureChanged();
			}
		});

		d_studyIndex.add(0);
		studiesAdded(0, studies.size() - 1);
		d_studies.addListDataListener(new ListDataListener() {
			public void contentsChanged(ListDataEvent e) {
				throw new UnsupportedOperationException();
			}
			public void intervalAdded(ListDataEvent e) {
				studiesAdded(e.getIndex0(), e.getIndex1());
			}
			public void intervalRemoved(ListDataEvent e) {
				studiesRemoved(e.getIndex0(), e.getIndex1());
			}
		});

		d_studyIdProxy = new ListPropertyChangeProxy<Study>(d_studies, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(Study.PROPERTY_ID)) {
					studyIdChanged((Study)evt.getSource());
				}
			}
		});

		d_treatmentIdProxy = new ListPropertyChangeProxy<Treatment>(treatments, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(Study.PROPERTY_ID)) {
					treatmentIdChanged((Treatment)evt.getSource());
				}
			}
		});
	}

	public MeasurementTableModel(DataSetModel model) {
		this(model.getStudies(), model.getTreatments(), model.getMeasurementType());
	}

	private void studyIdChanged(Study study) {
		for (int i = 0; i < d_studies.size(); ++i) {
			if (d_studies.get(i) == study) {
				fireTableCellUpdated(d_studyIndex.get(i), 0);
			}
		}
	}

	private void treatmentIdChanged(Treatment t) {
		for (int i = 0; i < d_studies.size(); ++i) {
			int j = d_studies.get(i).getMeasurements().indexOf(NetworkModel.findMeasurement(d_studies.get(i), t));
			if (j > 0) {
				fireTableCellUpdated(d_studyIndex.get(i) + j + 1, 0);
			}
		}
	}

	private void updateStudiesFrom(int idx, int delta) {
		for (int i = idx; i < d_studyIndex.size(); ++i) {
			d_studyIndex.set(i, d_studyIndex.get(i) + delta);
		}
	}

	private void studiesAdded(int start, int end) {
		int startIdx = d_studyIndex.get(start);
		int len = 0;
		for (int i = start; i <= end; ++i) {
			d_studyIndex.add(i, startIdx + len);
			len += d_studies.get(i).getMeasurements().size() + 1;
			d_studies.get(i).getMeasurements().addListDataListener(d_measurementListener);
		}
		updateStudiesFrom(end + 1, len);
		fireTableRowsInserted(startIdx, startIdx + len - 1);
	}

	private void studiesRemoved(int start, int end) {
		int startIdx = d_studyIndex.get(start);
		int len = d_studyIndex.get(end + 1) - startIdx;
		for (int i = end; i >= start; --i) {
			d_studyIndex.remove(i);
		}
		updateStudiesFrom(start, -len);
		fireTableRowsDeleted(startIdx, startIdx + len - 1);
	}

	private void treatmentsAdded(int study, int start, int end) {
		if (study < 0) {
			throw new IllegalStateException();
		}
		start += d_studyIndex.get(study) + 1;
		end += d_studyIndex.get(study) + 1;
		updateStudiesFrom(study + 1, end - start + 1);
		fireTableRowsInserted(start, end);
	}

	private void treatmentsRemoved(int study, int start, int end) {
		if (study < 0) {
			throw new IllegalStateException();
		}
		start += d_studyIndex.get(study) + 1;
		end += d_studyIndex.get(study) + 1;
		updateStudiesFrom(study + 1, start - end - 1);
		fireTableRowsDeleted(start, end);
	}

	private int findStudy(Object source) {
		for (int i = 0; i < d_studies.size(); ++i) {
			if (d_studies.get(i).getMeasurements() == source) {
				return i;
			}
		}
		return -1;
	}

	public int getRowCount() {
		return d_studyIndex.get(d_studies.size());
	}

	private DataType getMeasurementType() {
		return (DataType)d_measurementType.getValue();
	}

	public int getColumnCount() {
		switch (getMeasurementType()) {
			case RATE:
				return 3;
			case CONTINUOUS:
				return 4;
			case NONE:
				return 1;
		}
		throw new IllegalStateException();
	}

	@Override
	public Class<?> getColumnClass(int col) {
		if (col == 0) {
			return Object.class;
		} else {
			if (getMeasurementType().equals(DataType.RATE) || col == 3) {
				return Integer.class;
			} else {
				return Double.class;
			}
		}
	}

	private final String[] d_dichNames = { COLNAME_ID, COLNAME_RESPONDERS, COLNAME_SAMPLESIZE };
	private final String[] d_contNames = { COLNAME_ID, COLNAME_MEAN, COLNAME_STDDEV, COLNAME_SAMPLESIZE };
	private final String[] d_noneNames = { COLNAME_ID };

	@Override
	public String getColumnName(int col) {
		switch (getMeasurementType()) {
			case RATE:
				return d_dichNames[col];
			case CONTINUOUS:
				return d_contNames[col];
			case NONE:
				return d_noneNames[col];
		}
		throw new IllegalStateException();
	}

	private int findStudyIndex(int row) {
		for (int i = 0; i < d_studies.size(); ++i) {
			if (row >= d_studyIndex.get(i) && row < d_studyIndex.get(i + 1)) { // found the study
				return i;
			}
		}
		return -1;
	}

	public Object getValueAt(int row, int col) {
		int i = findStudyIndex(row);
		if (i < 0) {
			return null;
		}
		Study s = d_studies.get(i);
		if (row == d_studyIndex.get(i)) { // study description row
			if (col == 0) {
				return s;
			} else {
				return null;
			}
		} else {
			Treatment t = s.getMeasurements().get(row - d_studyIndex.get(i) - 1).getTreatment();
			if (col == 0) {
				return t;
			} else {
				return getValue(s, t, col);
			}
		}
	}

	private Object getValue(Study s, Treatment t, int col) {
		switch (getMeasurementType()) {
			case RATE:
				if (col == 1) {
					return NetworkModel.findMeasurement(s, t).getResponders();
				} else if (col == 2) {
					return NetworkModel.findMeasurement(s, t).getSampleSize();
				}
				break;
			case CONTINUOUS:
				if (col == 1) {
					return NetworkModel.findMeasurement(s, t).getMean();
				} else if (col == 2) {
					return NetworkModel.findMeasurement(s, t).getStdDev();
				} else if (col == 3) {
					return NetworkModel.findMeasurement(s, t).getSampleSize();
				}
				break;
			case NONE:
				break;
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		if (col == 0) {
			return false;
		}

		int i = findStudyIndex(row);
		if (i < 0 || row == d_studyIndex.get(i)) {
			return false;
		}

		return true;
	}

	@Override
	public void setValueAt(Object val, int row, int col) {
		int i = findStudyIndex(row);
		if (col == 0 || i < 0 || row == d_studyIndex.get(i)) {
			return;
		}
		int j = row - d_studyIndex.get(i) - 1;

		Study s = d_studies.get(i);
		Treatment t = s.getMeasurements().get(j).getTreatment();
		setValueAt(s, t, col, val);
		fireTableCellUpdated(row, col);
	}

	private void setValueAt(Study s, Treatment t, int col, Object val) {
		switch (getMeasurementType()) {
			case RATE:
				if (col == 1) {
					NetworkModel.findMeasurement(s, t).setResponders(((Integer)val));
				} else if (col == 2) {
					NetworkModel.findMeasurement(s, t).setSampleSize(((Integer)val));
				}
				break;
			case CONTINUOUS:
				if (col == 1) {
					NetworkModel.findMeasurement(s, t).setMean(((Double)val));
				} else if (col == 2) {
					NetworkModel.findMeasurement(s, t).setStdDev(((Double)val));
				} else if (col == 3) {
					NetworkModel.findMeasurement(s, t).setSampleSize(((Integer)val));
				}
				break;
			case NONE:
				break;
		}
	}
}
