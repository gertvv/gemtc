package org.drugis.mtc.gui.results;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.drugis.common.gui.table.EnhancedTable;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.presentation.ConsistencyWrapper;
import org.drugis.mtc.presentation.results.NetworkRelativeEffectTableModel;

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
		FormLayout layout = new FormLayout("pref:grow:fill", "p, 3dlu, p");
		PanelBuilder builder = new PanelBuilder(layout, this);
		final JTable table = new EnhancedTable(new NetworkRelativeEffectTableModel(d_treatments, d_wrapper), 200);
		table.setDefaultRenderer(Object.class, new NetworkRelativeEffectTableCellRenderer(d_isDichotomous, false));
		builder.add(new JScrollPane(table), cc.xy(1, 1));
	}

}
