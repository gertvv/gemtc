package org.drugis.mtc.gui.results;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.drugis.mtc.presentation.ConsistencyWrapper;
import org.drugis.mtc.presentation.NodeSplitWrapper;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class NodeSplitView extends JPanel {
	private static final long serialVersionUID = 1717678608791255147L;
	private NodeSplitWrapper<?> d_wrapper;

	public NodeSplitView(NodeSplitWrapper<?> wrapper, ConsistencyWrapper<?> consistency) {
		d_wrapper = wrapper;
		initComponents();
	}

	private void initComponents() {
		CellConstraints cc = new CellConstraints();
		FormLayout layout = new FormLayout("pref:grow:fill", "p, 3dlu, p, 3dlu, p, 3dlu, p");
		PanelBuilder builder = new PanelBuilder(layout, this);
		int row = 1;
		
		builder.addSeparator("Density", cc.xy(1, row));
		row += 2;
		builder.add(new JLabel("Density plot here"), cc.xy(1, row));
		row += 2;
		
		builder.addSeparator("Variance", cc.xy(1, row));
		row += 2;
		builder.add(new JScrollPane(ResultsComponentFactory.buildVarianceTable(d_wrapper)), cc.xy(1, row));
		row += 2;
	}

}
