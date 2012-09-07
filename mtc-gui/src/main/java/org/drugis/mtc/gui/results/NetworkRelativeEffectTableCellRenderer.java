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

package org.drugis.mtc.gui.results;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.drugis.mtc.model.Treatment;

public class NetworkRelativeEffectTableCellRenderer extends SummaryCellRenderer implements TableCellRenderer {
	private boolean d_showDescription;

	public NetworkRelativeEffectTableCellRenderer(boolean applyExpTransform) {
		this(applyExpTransform, true);
	}

	/**
	 * @param applyExpTransform For each quantile x, show e^x instead of x.
	 * @param showDescription For each treatment, show the description instead of the id.
	 */
	public NetworkRelativeEffectTableCellRenderer(boolean applyExpTransform, boolean showDescription) {
		super(applyExpTransform);
		d_showDescription = showDescription;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object cellContents, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component component =  super.getTableCellRendererComponent(
				table, cellContents, isSelected, hasFocus, row, column);

		if (cellContents instanceof Treatment) {
			final Treatment treatment = (Treatment) cellContents;
			component = (new DefaultTableCellRenderer()).getTableCellRendererComponent(
					table, getText(treatment), isSelected, hasFocus, row, column);
			component.setBackground(Color.LIGHT_GRAY);
		}

		((JComponent) component).setToolTipText(getTooltipAt(table.getModel(), row, column));
		return component;
	}

	public String getText(final Treatment treatment) {
		return d_showDescription ? treatment.getDescription() : treatment.getId();
	}

	public String getTooltipAt(TableModel model, int row, int col) {
		if (row == col) {
			return getText(getTreatment(model, row));
		}
		Treatment t1 = getTreatment(model, col);
		Treatment t2 = getTreatment(model, row);

		return "\"" + getText(t1)  + "\" relative to \"" + getText(t2) + "\"";
	}

	public Treatment getTreatment(TableModel model, int idx) {
		return (Treatment) model.getValueAt(idx, idx);
	}
}