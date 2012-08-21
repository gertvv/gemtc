package org.drugis.mtc.gui.results;

import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;

import org.drugis.common.gui.table.EnhancedTable;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.presentation.ConsistencyWrapper;
import org.drugis.mtc.presentation.MTCModelWrapper;
import org.drugis.mtc.presentation.NodeSplitWrapper;
import org.drugis.mtc.presentation.SimulationConsistencyWrapper;
import org.drugis.mtc.presentation.SimulationNodeSplitWrapper;
import org.drugis.mtc.presentation.results.NetworkRelativeEffectTableModel;
import org.drugis.mtc.presentation.results.NetworkVarianceTableModel;
import org.drugis.mtc.presentation.results.RankProbabilityDataset;
import org.drugis.mtc.presentation.results.RankProbabilityTableModel;
import org.drugis.mtc.util.EmpiricalDensityDataset;
import org.drugis.mtc.util.EmpiricalDensityDataset.PlotParameter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYDataset;

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

	public static JComponent buildNodeSplitDensityChart(final BasicParameter p, NodeSplitWrapper<?> wrapper, ConsistencyWrapper<?> consistency) {
		if (!(wrapper instanceof SimulationNodeSplitWrapper)) {
			return new JLabel("Can not build density plot based on saved results.");
		}
		final SimulationNodeSplitWrapper<?> splitWrapper = (SimulationNodeSplitWrapper<?>) wrapper;
		splitWrapper.getParameters();
		XYDataset dataset;
		final MCMCResults splitResults = splitWrapper.getModel().getResults();
		if(consistency instanceof SimulationConsistencyWrapper) {
			final SimulationConsistencyWrapper<?> consistencyWrapper = (SimulationConsistencyWrapper<?>) consistency;
			dataset = new EmpiricalDensityDataset(50, new PlotParameter(splitResults, splitWrapper.getDirectEffect()),
					new PlotParameter(splitResults, splitWrapper.getIndirectEffect()),
					new PlotParameter(consistencyWrapper.getModel().getResults(), p));
		} else {
			dataset = new EmpiricalDensityDataset(50, new PlotParameter(splitResults, splitWrapper.getDirectEffect()),
					new PlotParameter(splitResults, splitWrapper.getIndirectEffect()));
		}
		final JFreeChart chart = ChartFactory.createXYLineChart(
	            p.getName() + " density plot", "Relative Effect", "Density",
	            dataset, PlotOrientation.VERTICAL,
	            true, true, false
	        );

        return new ChartPanel(chart);
	}
}
