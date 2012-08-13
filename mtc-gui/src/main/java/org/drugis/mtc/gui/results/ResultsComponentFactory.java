package org.drugis.mtc.gui.results;

import java.util.List;

import javax.swing.JTable;

import org.drugis.common.gui.table.EnhancedTable;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.presentation.ConsistencyWrapper;
import org.drugis.mtc.presentation.MTCModelWrapper;
import org.drugis.mtc.presentation.results.NetworkRelativeEffectTableModel;
import org.drugis.mtc.presentation.results.NetworkVarianceTableModel;
import org.drugis.mtc.presentation.results.RankProbabilityDataset;
import org.drugis.mtc.presentation.results.RankProbabilityTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;

public class ResultsComponentFactory {

	public static JTable buildRelativeEffectsTable(final List<Treatment> treatments,
			final MTCModelWrapper<?> wrapper, final boolean isDichotomous,
			final boolean showDescription) {
		final JTable reTable = new EnhancedTable(new NetworkRelativeEffectTableModel(treatments, wrapper), 150);
		reTable.setDefaultRenderer(Object.class, new NetworkRelativeEffectTableCellRenderer(isDichotomous, showDescription));
		return reTable;
	}

	public static JTable buildVarianceTable(final MTCModelWrapper<?> wrapper) {
		final JTable varTable = new EnhancedTable(new NetworkVarianceTableModel(wrapper), 150);
		varTable.setDefaultRenderer(Object.class, new SummaryCellRenderer());
		return varTable;
	}

	public static ChartPanel buildRankProbabilityChart(final ConsistencyWrapper<?> wrapper) {
		CategoryDataset dataset = new RankProbabilityDataset(wrapper.getRankProbabilities(), wrapper);
		JFreeChart rankChart = ChartFactory.createBarChart("Rank Probability", "Treatment", "Probability",
				dataset, PlotOrientation.VERTICAL, true, true, false);
		final ChartPanel chartPanel = new ChartPanel(rankChart);
		return chartPanel;
	}

	public static EnhancedTable buildRankProbabilityTable(final ConsistencyWrapper<?> wrapper) {
		final EnhancedTable rankTable = EnhancedTable.createBare(new RankProbabilityTableModel(wrapper.getRankProbabilities(), wrapper));
		rankTable.setDefaultRenderer(Object.class, new SummaryCellRenderer());
		rankTable.autoSizeColumns();
		return rankTable;
	}

}
