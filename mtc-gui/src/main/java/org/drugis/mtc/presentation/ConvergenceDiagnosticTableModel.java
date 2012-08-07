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

package org.drugis.mtc.presentation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.drugis.mtc.Parameter;
import org.drugis.mtc.presentation.MCMCModelWrapper;
import org.drugis.mtc.summary.ConvergenceSummary;

import com.jgoodies.binding.value.ValueModel;

@SuppressWarnings("serial")
public class ConvergenceDiagnosticTableModel extends AbstractTableModel{

	private static final String NA = "N/A";
	private static final int COL_PARAM = 0;
	private static final int COL_ESTIMATE = 1;
	private Map<Parameter, ConvergenceSummary> d_summaries = new HashMap<Parameter, ConvergenceSummary>();
	private PropertyChangeListener d_listener;
	private static final NumberFormat s_format = new DecimalFormat("#.00");
	private final MCMCModelWrapper d_wrapper;

	public ConvergenceDiagnosticTableModel(MCMCModelWrapper wrapper, ValueModel modelConstructed) {		
		d_wrapper = wrapper;
		d_listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				fireTableDataChanged();
			}
		}; 
		modelConstructed.addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				initializeSummaries();
			}
		});
		if ((Boolean) modelConstructed.getValue()) {
			initializeSummaries();
		}
	}

	private void initializeSummaries() {
		for (Parameter p : d_wrapper.getParameters()) {
			ConvergenceSummary value = d_wrapper.getConvergenceSummary(p);
			if(value != null) { 
				value.addPropertyChangeListener(d_listener);
				d_summaries.put(p, value);
			}
		}
		fireTableDataChanged();
	}
	
	@Override
	public String getColumnName(int index) {
		if (index == COL_PARAM) {
			return "Parameter";
		} else if(index == COL_ESTIMATE) {
			return "PSRF";
		}
		return null;
	}

	public int getColumnCount() {
		return 2;
	}

	public int getRowCount() {
		return d_wrapper.getParameters().length;
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == COL_PARAM) {
			return getParameter(rowIndex);
		} else if (columnIndex == COL_ESTIMATE) {
			return getConvergence(rowIndex);
		}
		return null;
	}

	private String getConvergence(int rowIndex) {
		ConvergenceSummary summary = d_summaries.get(getParameter(rowIndex));
		if(summary == null) {
			return NA;
		}
		return summary.getDefined() ? formatNumber(summary) : NA;
	}

	private String formatNumber(ConvergenceSummary summary) {
		if (Double.isNaN(summary.getScaleReduction())) {
			return NA;
		}
		return s_format.format(summary.getScaleReduction());
	}

	private Parameter getParameter(int rowIndex) {
		return d_wrapper.getParameters()[rowIndex];
	}
}
