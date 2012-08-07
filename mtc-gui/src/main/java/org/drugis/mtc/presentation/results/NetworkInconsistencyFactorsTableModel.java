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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang.StringUtils;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.InconsistencyParameter;
import org.drugis.mtc.presentation.InconsistencyWrapper;
import org.drugis.mtc.summary.QuantileSummary;
import org.drugis.mtc.summary.Summary;

import com.jgoodies.binding.value.ValueModel;

@SuppressWarnings("serial")
public class NetworkInconsistencyFactorsTableModel extends AbstractTableModel {
	private static final String NA = "N/A";
	private PropertyChangeListener d_listener;
	private final InconsistencyWrapper<?> d_model;
	private boolean d_listenersAttached;
	private ValueModel d_modelConstructed;

	public NetworkInconsistencyFactorsTableModel(InconsistencyWrapper<?> networkModel, ValueModel modelConstructed) {
		d_model = networkModel;
		d_modelConstructed = modelConstructed;
		d_listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				fireTableDataChanged();
			}
		};
		
		if (d_modelConstructed.getValue().equals(true)) {
			attachListeners();
		}
		d_modelConstructed.addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getNewValue().equals(true)) {
					fireTableStructureChanged();					
					attachListeners();
				}
			}
		});
	}
	
	private void attachListeners() {
		if (d_listenersAttached) return;
		
		List<Parameter> parameterList = d_model.getInconsistencyFactors();
		for(Parameter p : parameterList ) {
			QuantileSummary summary = d_model.getQuantileSummary(p);
			summary.addPropertyChangeListener(d_listener);
		}
		d_listenersAttached = true;
	}
	
	@Override
	public Class<?> getColumnClass(int column) {
		return column == 0 ? String.class : Summary.class;
		
	}
	
	@Override
	public String getColumnName(int column) {
		return column == 0 ? "Cycle" : "Median (95% CrI)";
	}

	public int getColumnCount() {
		return 2;
	}

	public int getRowCount() {
		if(d_modelConstructed.getValue().equals(true)) {
			return d_model.getInconsistencyFactors().size();
		}
		return 0;
	}
	
	public Object getValueAt(int row, int col) {
		if (d_modelConstructed.getValue().equals(false)){
			return NA;
		}
		InconsistencyParameter ip = (InconsistencyParameter)d_model.getInconsistencyFactors().get(row);
		if(col == 0) {
			Set<String> descriptions = new TreeSet<String>();
			for(Treatment t : ip.getCycle()) { 
				descriptions.add(t.getDescription());
			}
			return StringUtils.join(descriptions, ", ");
		} else {
			return d_model.getQuantileSummary(ip);
		}
	}
}
