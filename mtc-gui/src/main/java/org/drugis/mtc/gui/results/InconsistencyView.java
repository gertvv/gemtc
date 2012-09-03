package org.drugis.mtc.gui.results;

import javax.swing.JPanel;
import javax.swing.JTable;

import org.drugis.common.gui.table.TablePanel;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.presentation.InconsistencyWrapper;

import com.jgoodies.binding.list.ObservableList;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class InconsistencyView extends JPanel {
	private static final long serialVersionUID = 3673513360852953378L;
	private ObservableList<Treatment> d_treatments;
	private InconsistencyWrapper<?> d_wrapper;
	private boolean d_isDichotomous;

	public InconsistencyView(ObservableList<Treatment> treatments, InconsistencyWrapper<?> wrapper, boolean isDichotomous) {
		d_treatments = treatments;
		d_wrapper = wrapper;
		d_isDichotomous = isDichotomous;
		initComponents();
	}

	private void initComponents() {
		CellConstraints cc = new CellConstraints();
		FormLayout layout = new FormLayout("pref:grow:fill", "p, 3dlu, p, 3dlu, p, 3dlu, p");
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
	}
}
