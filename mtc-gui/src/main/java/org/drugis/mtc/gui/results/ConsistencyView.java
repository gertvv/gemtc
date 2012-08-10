package org.drugis.mtc.gui.results;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.drugis.common.gui.table.EnhancedTable;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.presentation.ConsistencyWrapper;
import org.drugis.mtc.presentation.results.NetworkRelativeEffectTableModel;
import org.drugis.mtc.presentation.results.NetworkVarianceTableModel;
import org.drugis.mtc.presentation.results.RankProbabilityDataset;
import org.drugis.mtc.presentation.results.RankProbabilityTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;

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
		final JTable reTable = new EnhancedTable(new NetworkRelativeEffectTableModel(d_treatments, d_wrapper), 150);
		reTable.setDefaultRenderer(Object.class, new NetworkRelativeEffectTableCellRenderer(d_isDichotomous, false));
		builder.add(new JScrollPane(reTable), cc.xy(1, row));
		row += 2;
		
		builder.addSeparator("Variance", cc.xy(1, row));
		row += 2;
		final JTable varTable = new EnhancedTable(new NetworkVarianceTableModel(d_wrapper), 150);
		varTable.setDefaultRenderer(Object.class, new SummaryCellRenderer());
		builder.add(new JScrollPane(varTable), cc.xy(1, row));
		row += 2;
		
		builder.addSeparator("Rank probabilities", cc.xy(1, row));
		row += 2;
		CategoryDataset dataset = new RankProbabilityDataset(d_wrapper.getRankProbabilities(), d_wrapper);
		JFreeChart rankChart = ChartFactory.createBarChart("Rank Probability", "Treatment", "Probability",
				dataset, PlotOrientation.VERTICAL, true, true, false);
		builder.add(new ChartPanel(rankChart), cc.xy(1, row));
		row += 2;
		final EnhancedTable rankTable = EnhancedTable.createBare(new RankProbabilityTableModel(d_wrapper.getRankProbabilities(), d_wrapper));
		rankTable.setDefaultRenderer(Object.class, new SummaryCellRenderer());
		rankTable.autoSizeColumns();
		builder.add(new JScrollPane(rankTable), cc.xy(1, row));
		row += 2;
	}

}
