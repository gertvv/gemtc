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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.drugis.mtc.Parameter;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.presentation.ConsistencyWrapper;
import org.drugis.mtc.presentation.MTCModelWrapper;
import org.drugis.mtc.presentation.NodeSplitWrapper;
import org.drugis.mtc.summary.NodeSplitPValueSummary;
import org.drugis.mtc.summary.QuantileSummary;
import org.drugis.mtc.summary.Summary;

@SuppressWarnings("serial")
public class NodeSplitResultsTableModel extends AbstractTableModel {

	private static final String NA = "N/A";
	private static final int COL_NAME = 0;
	private static final int COL_DIRECT_EFFECT = 1;
	private static final int COL_INDIRECT_EFFECT = 2;
	private static final int COL_OVERALL = 3;
	private static final int COL_P_VALUE = 4;
	private static final int N_COLS = 5;
	private Map<Parameter, Summary> d_quantileSummaries = new HashMap<Parameter, Summary>();
	private Map<Parameter, Summary> d_pValueSummaries = new HashMap<Parameter, Summary>();
	private PropertyChangeListener d_listener;
	private final ConsistencyWrapper<?> d_consistencyModel;
	private final List<NodeSplitWrapper<?>> d_nodeSplitModels;
	
	public NodeSplitResultsTableModel(ConsistencyWrapper<?> consistencyModel, 
			List<NodeSplitWrapper<?>> nodeSplitModels) {
		d_consistencyModel = consistencyModel;
		d_nodeSplitModels = nodeSplitModels;
		
		d_listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				fireTableDataChanged();
			}
		};
		
		if(d_nodeSplitModels.size() > 0) {
			initializeTable();
		}
	}

	private void initializeTable() {
		for (NodeSplitWrapper<?> m : d_nodeSplitModels) {
			attachQuantileSummary(d_consistencyModel, m.getSplitNode());
			attachQuantileSummary(m, m.getDirectEffect());
			attachQuantileSummary(m, m.getIndirectEffect());
			
			NodeSplitPValueSummary valuePvalue = m.getNodeSplitPValueSummary();
			valuePvalue.addPropertyChangeListener(d_listener);
			d_pValueSummaries.put(m.getSplitNode(), valuePvalue);
		}
	}

	private void attachQuantileSummary(MTCModelWrapper<?> model, Parameter param) {
		QuantileSummary summary = model.getQuantileSummary(param);
		if(summary != null) { 
			summary.addPropertyChangeListener(d_listener); 
		}
		d_quantileSummaries.put(param, summary);
	}
	
	@Override
	public String getColumnName(int index) {
		switch(index) {
			case COL_NAME : return "Name"; 
			case COL_DIRECT_EFFECT : return "Direct Effect";
			case COL_INDIRECT_EFFECT : return "Indirect Effect";
			case COL_OVERALL : return "Overall";
			case COL_P_VALUE : return "P-Value";
			default: return null;
		}
	}
	
	public int getColumnCount() {
		return N_COLS;
	}

	public int getRowCount() {
		return d_nodeSplitModels.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == COL_NAME) {
			return getDescription((BasicParameter)d_nodeSplitModels.get(rowIndex).getSplitNode());
		} else if (columnIndex >= COL_DIRECT_EFFECT && columnIndex <= COL_P_VALUE) {
			return getSummary(rowIndex, columnIndex);
		} 
		return null;
	}

	private Object getSummary(int rowIndex, int columnIndex) {
		switch(columnIndex) {
			case COL_DIRECT_EFFECT : return d_quantileSummaries.get(d_nodeSplitModels.get(rowIndex).getDirectEffect());
			case COL_INDIRECT_EFFECT : return d_quantileSummaries.get(d_nodeSplitModels.get(rowIndex).getIndirectEffect());
			case COL_OVERALL : return d_quantileSummaries.get(d_nodeSplitModels.get(rowIndex).getSplitNode());
			case COL_P_VALUE : return d_pValueSummaries.get(d_nodeSplitModels.get(rowIndex).getSplitNode()); 
			default : return NA;
		}
	}

	private String getDescription(BasicParameter p) { 
		return p.getBaseline().getDescription() + ", " + p.getSubject().getDescription();
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch(columnIndex) {
			case COL_NAME : return String.class;
			case COL_DIRECT_EFFECT : return QuantileSummary.class;
			case COL_INDIRECT_EFFECT : return QuantileSummary.class;
			case COL_OVERALL : return QuantileSummary.class;
			case COL_P_VALUE : return NodeSplitPValueSummary.class;  
			default : return Object.class;
		}
	}
}
