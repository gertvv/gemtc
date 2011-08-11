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

import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;

public class MeasurementTableModel extends AbstractTableModel {
	private ObservableList<StudyModel> d_studies;
	private List<Integer> d_studyIndex = new ArrayList<Integer>();

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

	public MeasurementTableModel(ObservableList<StudyModel> studies) {
		d_studies = studies;
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

	@Override
	public int getRowCount() {
		return d_studyIndex.get(d_studies.size());
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public String getColumnName(int col) {
		if (col == 0) {
			return "";
		}
		if (col == 1) {
			return "Responders";
		}
		if (col == 2) {
			return "Sample size";
		}
		throw new IndexOutOfBoundsException();
	}

	private int findStudyIndex(int row) {
		for (int i = 0; i < d_studies.size(); ++i) {
			if (row >= d_studyIndex.get(i) && row < d_studyIndex.get(i + 1)) { // found the study
				return i;
			}
		}
		return -1;
	}

	@Override
	public Object getValueAt(int row, int col) {
		int i = findStudyIndex(row);
		if (i < 0) {
			return null;
		}
		if (row == d_studyIndex.get(i)) { // study description row
			if (col == 0) {
				return d_studies.get(i);
			} else {
				return null;
			}
		} else {
			if (col == 0) {
				return d_studies.get(i).getTreatments().get(row - d_studyIndex.get(i) - 1);
			} else {
				return 0;
			}
		}
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
}
