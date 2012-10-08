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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.drugis.common.gui.table.TablePanel;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.presentation.InconsistencyWrapper;
import org.drugis.mtc.presentation.MCMCPresentation;

import com.jgoodies.binding.list.ObservableList;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class InconsistencyView extends JPanel {
	private static final long serialVersionUID = 3673513360852953378L;
	private ObservableList<Treatment> d_treatments;
	private InconsistencyWrapper<?> d_wrapper;
	private boolean d_isDichotomous;
	private MCMCPresentation d_presentation;

	public InconsistencyView(ObservableList<Treatment> treatments,
			MCMCPresentation presentation,
			InconsistencyWrapper<?> wrapper,
			boolean isDichotomous) {
		d_treatments = treatments;
		d_presentation = presentation;
		d_wrapper = wrapper;
		d_isDichotomous = isDichotomous;
		initComponents();
	}

	private void initComponents() {
		CellConstraints cc = new CellConstraints();
		FormLayout layout = new FormLayout("pref:grow:fill", "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p");
		PanelBuilder builder = new PanelBuilder(layout, this);
		int row = 1;

		builder.addSeparator("Relative effects", cc.xy(1, row));
		row += 2;
		final JTable reTable = ResultsComponentFactory.buildRelativeEffectsTable(d_treatments, d_wrapper, d_isDichotomous, true);
		builder.add(new TablePanel(reTable), cc.xy(1, row));
		row += 2;

		builder.addSeparator("Inconsistency Factors", cc.xy(1, row));
		row += 2;
		final JTable inconTable = ResultsComponentFactory.buildInconsistencyFactors(d_wrapper, d_presentation.isModelConstructed());

		d_presentation.isModelConstructed().addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent event) {
				if (event.getNewValue().equals(true)) {
					final Runnable r = new Runnable() {
						public void run() {
							inconTable.doLayout();
						}
					};
					SwingUtilities.invokeLater(r);
				}
			}
		});

		builder.add(new TablePanel(inconTable), cc.xy(1, row));
		row += 2;

		builder.addSeparator("Variance", cc.xy(1, row));
		row += 2;
		builder.add(new TablePanel(ResultsComponentFactory.buildVarianceTable(d_wrapper)), cc.xy(1, row));
		row += 2;
	}
}
