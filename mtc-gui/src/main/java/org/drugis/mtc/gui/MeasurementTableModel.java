/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
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

import java.util.List;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import com.jgoodies.binding.list.ObservableList;
import com.jgoodies.binding.value.ValueModel;

import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class MeasurementTableModel extends AbstractTableModel {
	public static final String COLNAME_STDDEV = "Standard deviation";
	public static final String COLNAME_MEAN = "Mean";
	public static final String COLNAME_SAMPLESIZE = "Sample size";
	public static final String COLNAME_RESPONDERS = "Responders";
	public static final String COLNAME_ID = "";

	private static final long serialVersionUID = -1186064425875064988L;

	private ValueModel d_measurementType;
	private ObservableList<StudyModel> d_studies;
	private List<Integer> d_studyIndex = new ArrayList<Integer>();
	@SuppressWarnings("unused") private ListPropertyChangeProxy<StudyModel> d_studyIdProxy;
	@SuppressWarnings("unused") private ListPropertyChangeProxy<TreatmentModel> d_treatmentIdProxy;

	private ListDataListener d_treatmentListener = new ListDataListener() {
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
			ObservableList<StudyModel> studies,
			ObservableList<TreatmentModel> treatments,
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

		d_studyIdProxy = new ListPropertyChangeProxy<StudyModel>(d_studies, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(StudyModel.PROPERTY_ID)) {
					studyIdChanged((StudyModel)evt.getSource());
				}
			}
		});

		d_treatmentIdProxy = new ListPropertyChangeProxy<TreatmentModel>(treatments, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(StudyModel.PROPERTY_ID)) {
					treatmentIdChanged((TreatmentModel)evt.getSource());
				}
			}
		});
	}

	public MeasurementTableModel(DataSetModel model) {
		this(model.getStudies(), model.getTreatments(), model.getMeasurementType());
	}

	private void studyIdChanged(StudyModel study) {
		for (int i = 0; i < d_studies.size(); ++i) {
			if (d_studies.get(i) == study) {
				fireTableCellUpdated(d_studyIndex.get(i), 0);
			}
		}
	}

	private void treatmentIdChanged(TreatmentModel t) {
		for (int i = 0; i < d_studies.size(); ++i) {
			int j = d_studies.get(i).getTreatments().indexOf(t);
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
			len += d_studies.get(i).getTreatments().size() + 1;
			d_studies.get(i).getTreatments().addListDataListener(d_treatmentListener);
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
			if (d_studies.get(i).getTreatments() == source) {
				return i;
			}
		}
		return -1;
	}

	public int getRowCount() {
		return d_studyIndex.get(d_studies.size());
	}

	private MeasurementType getMeasurementType() {
		return (MeasurementType)d_measurementType.getValue();
	}

	public int getColumnCount() {
		switch (getMeasurementType()) {
			case DICHOTOMOUS:
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
			if (getMeasurementType().equals(MeasurementType.DICHOTOMOUS) || col == 3) {
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
			case DICHOTOMOUS:
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
		StudyModel s = d_studies.get(i);
		if (row == d_studyIndex.get(i)) { // study description row
			if (col == 0) {
				return s;
			} else {
				return null;
			}
		} else {
			TreatmentModel t = s.getTreatments().get(row - d_studyIndex.get(i) - 1);
			if (col == 0) {
				return t;
			} else {
				return getValue(s, t, col);
			}
		}
	}

	private Object getValue(StudyModel s, TreatmentModel t, int col) {
		switch (getMeasurementType()) {
			case DICHOTOMOUS:
				if (col == 1) {
					return s.getResponders(t);
				} else if (col == 2) {
					return s.getSampleSize(t);
				}
				break;
			case CONTINUOUS:
				if (col == 1) {
					return s.getMean(t);
				} else if (col == 2) {
					return s.getStdDev(t);
				} else if (col == 3) {
					return s.getSampleSize(t);
				}
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

		StudyModel s = d_studies.get(i);
		TreatmentModel t = s.getTreatments().get(j);
		setValueAt(s, t, col, val);
	}

	private void setValueAt(StudyModel s, TreatmentModel t, int col, Object val) {
		switch (getMeasurementType()) {
			case DICHOTOMOUS:
				if (col == 1) {
					s.setResponders(t, (Integer)val);
				} else if (col == 2) {
					s.setSampleSize(t, (Integer)val);
				}
				break;
			case CONTINUOUS:
				if (col == 1) {
					s.setMean(t, (Double)val);
				} else if (col == 2) {
					s.setStdDev(t, (Double)val);
				} else if (col == 3) {
					s.setSampleSize(t, (Integer)val);
				}
				break;
		}
	}
}
