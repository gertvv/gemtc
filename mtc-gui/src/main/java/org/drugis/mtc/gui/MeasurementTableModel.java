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

import javax.swing.table.AbstractTableModel;

import com.jgoodies.binding.list.ObservableList;

public class MeasurementTableModel extends AbstractTableModel {
	private ObservableList<StudyModel> d_studies;

	public MeasurementTableModel(ObservableList<StudyModel> studies) {
		d_studies = studies;
	}

	@Override
	public int getRowCount() {
		int cnt = 0;
		for (StudyModel s : d_studies) {
			++cnt;
			cnt += s.getTreatments().size();
		}
		return cnt;
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

	@Override
	public Object getValueAt(int row, int col) {
		int cnt = 0;
		for (StudyModel s : d_studies) {
			if (cnt == row) {
				if (col == 0) {
					return s;
				} else {
					return null;
				}
			}
			++cnt;
			for (TreatmentModel t : s.getTreatments()) {
				if (cnt == row) {
					if (col == 0) {
						return t;
					} else {
						return 0;
					}
				}
				++cnt;
			}
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		int cnt = 0;
		for (StudyModel s : d_studies) {
			if (cnt == row) {
				return false;
			}
			++cnt;
			for (TreatmentModel t : s.getTreatments()) {
				if (cnt == row) {
					if (col == 0) {
						return false;
					} else {
						return true;
					}
				}
				++cnt;
			}
		}
		return false;
	}
}
