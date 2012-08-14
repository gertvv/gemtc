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
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.presentation.MTCModelWrapper;
import org.drugis.mtc.summary.QuantileSummary;

@SuppressWarnings("serial")
public class NetworkRelativeEffectTableModel extends AbstractTableModel {
	private final List<Treatment> d_treatments;
	MTCModelWrapper<?> d_wrapper;
	private final PropertyChangeListener d_listener;
	
	public static <E> NetworkRelativeEffectTableModel build(List<E> treatments, MTCModelWrapper<E> wrapper) {
		List<Treatment> list = new ArrayList<Treatment>();
		for (E e : treatments) {
			list.add(wrapper.forwardMap(e));
		}	
		return new NetworkRelativeEffectTableModel(list, wrapper);
	}
	
	public NetworkRelativeEffectTableModel(List<Treatment> treatments, MTCModelWrapper<?> wrapper) {
		d_treatments = treatments;
		d_wrapper = wrapper;
		d_listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				fireTableDataChanged();
			}
		};
		
		// Listen to summaries
		for(Treatment t1 : d_treatments) {
			for (Treatment t2 : d_treatments) {
				if (!t1.equals(t2)) {
					attachListener(wrapper, t1, t2);
				}
			}
		}
	}

	private void attachListener(MTCModelWrapper<?> networkModel, Treatment t1, Treatment t2) {
		QuantileSummary quantileSummary = getSummary(t1, t2);
		if(quantileSummary != null) {
			quantileSummary.addPropertyChangeListener(d_listener);
		}
	}

	public int getColumnCount() {
		return d_treatments.size();
	}

	public int getRowCount() {
		return d_treatments.size();
	}
	
	public Object getValueAt(int row, int col) {
		if (row == col) {
			return d_treatments.get(row);
		}
		return getSummary(d_treatments.get(row), d_treatments.get(col));
	}
	
	private QuantileSummary getSummary(final Treatment t1, final Treatment t2) {
		return d_wrapper.getQuantileSummary(d_wrapper.getRelativeEffect(t1, t2));
	}
}
