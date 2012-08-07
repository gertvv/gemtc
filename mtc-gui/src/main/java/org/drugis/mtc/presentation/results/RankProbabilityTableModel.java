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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.table.AbstractTableModel;

import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.presentation.MTCModelWrapper;
import org.drugis.mtc.summary.RankProbabilitySummary;

public class RankProbabilityTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 6045183840617200792L;
	private final RankProbabilitySummary d_summary;
	private final MTCModelWrapper<?> d_model;

	public RankProbabilityTableModel(RankProbabilitySummary summary) { 
		this(summary, null);
	}
	
	public RankProbabilityTableModel(RankProbabilitySummary summary, MTCModelWrapper<?> model) {
		d_summary = summary;
		d_model = model;
		d_summary.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				fireTableDataChanged();
			}
		});
	}

	public int getColumnCount() {
		return d_summary.getTreatments().size() + 1;
	}

	public int getRowCount() {
		return d_summary.getTreatments().size();
	}
	
	@Override
	public String getColumnName(int column) {
		if (column == 0) {
			return "Drug";
		} else {
			return "Rank " + column;
		}
	}
	
	@Override
	public Class<?> getColumnClass(int column) {
		if (column == 0) {
			return String.class;
		} else {
			return Double.class;
		}
	}
	
	public Object getValueAt(int rowIndex, int columnIndex) {
		Treatment treatment = d_summary.getTreatments().get(rowIndex);
		if (columnIndex == 0) {
			if(d_model != null) { 
				return treatment.getDescription();
			}
			return treatment.getId();
		} else {
			return d_summary.getDefined() ? d_summary.getValue(treatment, columnIndex) : "";
		}
	}
}
