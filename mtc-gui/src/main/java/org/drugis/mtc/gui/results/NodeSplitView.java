package org.drugis.mtc.gui.results;

import javax.swing.JPanel;

import org.drugis.common.gui.table.TablePanel;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.presentation.ConsistencyWrapper;
import org.drugis.mtc.presentation.NodeSplitWrapper;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class NodeSplitView extends JPanel {
	private static final long serialVersionUID = 1717678608791255147L;
	private NodeSplitWrapper<?> d_nsWrapper;
	private ConsistencyWrapper<?> d_consistencyWrapper;
	private BasicParameter d_parameter;

	public NodeSplitView(BasicParameter parameter, NodeSplitWrapper<?> nsWrapper, ConsistencyWrapper<?> consistencyWrapper) {
		d_parameter = parameter;
		d_nsWrapper = nsWrapper;
		d_consistencyWrapper = consistencyWrapper;
		initComponents();
	}

	private void initComponents() {
		CellConstraints cc = new CellConstraints();
		FormLayout layout = new FormLayout("pref:grow:fill", "p, 3dlu, p, 3dlu, p, 3dlu, p");
		PanelBuilder builder = new PanelBuilder(layout, this);
		int row = 1;

		builder.addSeparator("Density", cc.xy(1, row));
		row += 2;
		builder.add(ResultsComponentFactory.buildNodeSplitDensityChart(d_parameter, d_nsWrapper, d_consistencyWrapper), cc.xyw(1, row, layout.getColumnCount()));
		row += 2;

		builder.addSeparator("Variance", cc.xy(1, row));
		row += 2;
		builder.add(new TablePanel(ResultsComponentFactory.buildVarianceTable(d_nsWrapper)), cc.xy(1, row));
		row += 2;
	}

}
