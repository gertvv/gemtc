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

package org.drugis.mtc.gui;

import java.awt.Color;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import org.drugis.common.gui.task.TaskProgressBar;
import org.drugis.common.threading.SimpleSuspendableTask;
import org.drugis.common.threading.ThreadHandler;
import org.drugis.mtc.MCMCModel;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;
import org.drugis.mtc.MCMCSettings;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.convergence.GelmanRubinConvergence;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

@SuppressWarnings("serial")
public class ConvergencePlotsDialog extends JDialog {
	final static int DATA_SCALE = 200;
	private final XYSeries d_rHatSeries;
	private final XYSeries d_vHatSeries;
	private final XYSeries d_wSeries;
	private final SimpleSuspendableTask d_task;

	public ConvergencePlotsDialog(final JFrame main, final MCMCSettings settings, final MCMCModel mcmcModel, final Parameter p) {
		super(main, p + " convergence diagnostics", false);
		d_rHatSeries = new XYSeries("R-Hat");
		d_vHatSeries = new XYSeries("sqrt(vHat)");
		d_wSeries = new XYSeries("sqrt(W)");
		d_task = createTask(mcmcModel.getResults(), settings, p);

		super.add(new JScrollPane(createPanel()));

		mcmcModel.getResults().addResultsListener(new MCMCResultsListener() {
			@Override
			public void resultsEvent(final MCMCResultsEvent event) {
				fillDataSets();
			}
		});

		if(mcmcModel.getResults().getNumberOfSamples() > 0) {
			fillDataSets();
		}
	}

	private JPanel createPanel() {
		final FormLayout layout = new FormLayout(
				"pref:grow:fill",
				"p, 3dlu, p, 3dlu, p");
		final PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		final CellConstraints cc = new CellConstraints();

		final XYSeriesCollection datasetRhat = new XYSeriesCollection();
		final XYSeriesCollection datasetVhatVsW = new XYSeriesCollection();
		datasetRhat.addSeries(d_rHatSeries);
		datasetVhatVsW.addSeries(d_vHatSeries);
		datasetVhatVsW.addSeries(d_wSeries);
		final JFreeChart rHatChart = createRhatChart(datasetRhat);
		final JFreeChart vHatvsWChart = createVhatVsWChart(datasetVhatVsW);
		final ChartPanel chartPanelRhat = new ChartPanel(rHatChart);
		final ChartPanel chartPanelVhatVsW = new ChartPanel(vHatvsWChart);
		chartPanelRhat.setVisible(true);
		chartPanelVhatVsW.setVisible(true);

		final JProgressBar bar = new TaskProgressBar(d_task);

		builder.add(bar, cc.xy(1, 1));
		builder.add(chartPanelRhat, cc.xy(1, 3));
		builder.add(chartPanelVhatVsW, cc.xy(1, 5));
		return builder.getPanel();
	}

	private void fillDataSets() {
		ThreadHandler.getInstance().scheduleTask(d_task);
	}

	private SimpleSuspendableTask createTask(final MCMCResults results,	final MCMCSettings settings, final Parameter p) {
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				final int noResults = results.getNumberOfSamples();
				final int resolution = noResults / DATA_SCALE;

				for (int i = resolution; i <= noResults; i += resolution) {
					final int iter = i * settings.getThinningInterval();
					d_rHatSeries.add(iter, GelmanRubinConvergence.diagnose(results, p, i));
					d_vHatSeries.add(iter, GelmanRubinConvergence.calculatePooledVariance(results, p, i));
					d_wSeries.add(iter, GelmanRubinConvergence.calculateWithinChainVariance(results, p, i));
				}
			}
		};
		final SimpleSuspendableTask task = new SimpleSuspendableTask(r, "Computing results");
		return task;
	}

	private JFreeChart createRhatChart(final XYDataset dataset) {
		final JFreeChart RhatChart = ChartFactory.createXYLineChart(
				"Iterative PSRF Plot",
				"Iteration No.", "R-Hat(p)",
				dataset, PlotOrientation.VERTICAL,
				false, true, false
				);

		RhatChart.setBackgroundPaint(Color.white);
		final XYPlot RhatPlot = RhatChart.getXYPlot();
		RhatPlot.setDomainGridlinePaint(Color.white);
		RhatPlot.setRangeGridlinePaint(Color.white);

        final NumberAxis rangeAxis = (NumberAxis) RhatPlot.getRangeAxis();
        rangeAxis.setAutoRange(true);
        rangeAxis.setAutoRangeIncludesZero(false);

        return RhatChart;
	}

	private JFreeChart createVhatVsWChart(final XYDataset dataset) {
		final JFreeChart VhatVsWChart = ChartFactory.createXYLineChart(
				"Plots of sqrt(vHat) and sqrt(W)",
				"Iteration No.", "Variance Estimates",
				dataset, PlotOrientation.VERTICAL,
				true, true, false
				);

		VhatVsWChart.setBackgroundPaint(Color.white);
		final XYPlot VhatVsWPlot = VhatVsWChart.getXYPlot();
		VhatVsWPlot.setDomainGridlinePaint(Color.white);
		VhatVsWPlot.setRangeGridlinePaint(Color.white);

		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesPaint(0, Color.red);
		renderer.setSeriesPaint(1, Color.blue);
		renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesShapesVisible(1, false);
        VhatVsWPlot.setRenderer(renderer);

        return VhatVsWChart;
	}
}
