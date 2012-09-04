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
		final JTable reTable = ResultsComponentFactory.buildRelativeEffectsTable(d_treatments, d_wrapper, d_isDichotomous, false);
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
