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

import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTable;

import org.drugis.common.gui.table.TablePanel;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.presentation.ConsistencyWrapper;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ConsistencyView extends JPanel {
	private static final long serialVersionUID = 585117431275332905L;

	private List<Treatment> d_treatments;
	private ConsistencyWrapper<?> d_wrapper;
	private boolean d_isDichotomous;

	public ConsistencyView(List<Treatment> treatments, ConsistencyWrapper<?> wrapper, boolean isDichotomous) {
		d_treatments = treatments;
		d_wrapper = wrapper;
		d_isDichotomous = isDichotomous;
		initComponents();
	}

	private void initComponents() {
		CellConstraints cc = new CellConstraints();
		FormLayout layout = new FormLayout("pref:grow:fill", "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p");
		PanelBuilder builder = new PanelBuilder(layout, this);
		int row = 1;

		builder.addSeparator("Relative effects", cc.xy(1, row));
		row += 2;
		final JTable reTable = ResultsComponentFactory.buildRelativeEffectsTable(d_treatments, d_wrapper, d_isDichotomous, true);
		builder.add(new TablePanel(reTable), cc.xy(1, row));
		row += 2;

		builder.addSeparator("Variance", cc.xy(1, row));
		row += 2;
		builder.add(new TablePanel(ResultsComponentFactory.buildVarianceTable(d_wrapper)), cc.xy(1, row));
		row += 2;

		builder.addSeparator("Rank probabilities", cc.xy(1, row));
		row += 2;
		builder.add(ResultsComponentFactory.buildRankProbabilityChart(d_wrapper), cc.xy(1, row));
		row += 2;
		builder.add(new TablePanel(ResultsComponentFactory.buildRankProbabilityTable(d_wrapper)), cc.xy(1, row));
		row += 2;
	}
}
