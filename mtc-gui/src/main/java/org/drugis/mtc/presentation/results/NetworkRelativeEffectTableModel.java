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

import javax.swing.table.AbstractTableModel;

import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.presentation.MTCModelWrapper;
import org.drugis.mtc.summary.QuantileSummary;

@SuppressWarnings("serial")
public class NetworkRelativeEffectTableModel<TreatmentType> extends AbstractTableModel {
	private final List<TreatmentType> d_treatments;
	MTCModelWrapper<TreatmentType> d_networkModel;
	private final PropertyChangeListener d_listener;
	
	public NetworkRelativeEffectTableModel(List<TreatmentType> treatments, MTCModelWrapper<TreatmentType> networkModel) {
		d_treatments = treatments;
		d_networkModel = networkModel;
		d_listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				fireTableDataChanged();
			}
		};
		
		// Listen to summaries
		for(TreatmentType d1 : d_treatments) {
			for (TreatmentType d2 : d_treatments) {
				if (!d1.equals(d2)) {
					attachListener(networkModel, d1, d2);
				}
			}
		}
	}

	private void attachListener(MTCModelWrapper<TreatmentType> networkModel, TreatmentType d1, TreatmentType d2) {
		QuantileSummary quantileSummary = getSummary(d1, d2);
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
	
	public String getDescriptionAt(int row, int col) {
		if (row == col) {
			return null;
		}
		Treatment t1 = d_networkModel.forwardMap(d_treatments.get(col));
		Treatment t2 = d_networkModel.forwardMap(d_treatments.get(row));

		return "\"" + t1.getDescription() + "\" relative to \"" + t2.getDescription() + "\"";
	}

	public Object getValueAt(int row, int col) {
		if (row == col) {
			return d_treatments.get(row);
		}
		return getSummary(d_treatments.get(row), d_treatments.get(col));
	}
	
	private QuantileSummary getSummary(final TreatmentType d1, final TreatmentType d2) {
		return d_networkModel.getQuantileSummary(d_networkModel.getRelativeEffect(d1, d2));
	}
}
