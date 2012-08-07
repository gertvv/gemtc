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

package org.drugis.mtc.gui.progressgraph;

import java.awt.Dimension;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;
import org.drugis.common.threading.Task;
import org.drugis.mtc.MCMCModel;
import org.drugis.mtc.gui.progressgraph.GraphSimpleNode.GraphSimpleNodeType;
import org.drugis.mtc.presentation.MCMCPresentation;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

public class ProgressGraph extends JPanel {
	private static final long serialVersionUID = -3792386552084832239L;
	private int d_numCols;
	private int d_numTotalRows;
	private int d_numMainRows;
	private int d_numberOfChains;
	private int d_barWidth;
	private int d_arrowSize;
	private int d_edgeLength;
	private int d_circleDiameter;
	private Dimension d_gridCellSize;
	private int d_cellHeight;
	private final MCMCPresentation d_model;

	public ProgressGraph(final MCMCPresentation model, JFrame main) {
		d_model = model;
		d_cellHeight = (int)new JProgressBar().getPreferredSize().getHeight() + 4;
		d_gridCellSize = new Dimension(140, d_cellHeight);
		d_circleDiameter = 20;
		d_edgeLength = 30;
		d_arrowSize = 10;
		d_barWidth = 9;
		d_numberOfChains = model.getWrapper().getSettings().getNumberOfChains();
		d_numMainRows = (d_numberOfChains - 1) * 2 + 1;
		d_numTotalRows = d_numMainRows + 2;
		d_numCols = 17;
		
		
		JPanel progressGraph = createPanel();

		add(progressGraph);
	}
	
	public JPanel createPanel() { 
		final FormLayout layout = new FormLayout(
				createFormSpec("pref", d_numCols),
				"p, " + createFormSpec("3dlu, p", d_numTotalRows - 1));
		CellConstraints cc = new CellConstraints();
		JPanel progressPanel = new JPanel(layout);
		Dimension cellSize = new Dimension(d_edgeLength, d_arrowSize);
		Dimension circleSize = new Dimension(d_circleDiameter,d_circleDiameter);
		
		for(int i = 0; i < d_numberOfChains; ++i) {
			int rowIdx = (2 * i) + 1;
			Task tuningTask = d_model.getModel().getActivityTask().getModel().getStateByName(MCMCModel.TUNING_CHAIN_PREFIX + i);
			progressPanel.add(new GraphLine(cellSize, 2, SwingConstants.EAST), cc.xy(6, rowIdx));
			progressPanel.add(new GraphProgressNode(d_gridCellSize, tuningTask), cc.xy(7, rowIdx));
			Task simulationTask = d_model.getModel().getActivityTask().getModel().getStateByName(MCMCModel.SIMULATION_CHAIN_PREFIX + i);
			progressPanel.add(new GraphLine(new Dimension(d_edgeLength * 2, d_arrowSize), 2, SwingConstants.EAST), cc.xy(9, rowIdx));
			progressPanel.add(new GraphProgressNode(d_gridCellSize,  simulationTask), cc.xy(10, rowIdx));
			progressPanel.add(new GraphLine(cellSize, 2, SwingConstants.EAST), cc.xy(11, rowIdx));	
		}
		
		/** Placement needed for the calculated preferred size */
		progressPanel.add(new GraphSimpleNode(circleSize, GraphSimpleNodeType.START), centerCell(cc, d_numMainRows, 1));
		progressPanel.add(new GraphLine(cellSize, 2, SwingConstants.EAST), centerCell(cc, d_numMainRows, 2));
		Task startTask = d_model.getModel().getActivityTask().getModel().getStateByName(MCMCModel.STARTING_SIMULATION_PHASE);
		progressPanel.add(new GraphProgressNode(d_gridCellSize, startTask, false), centerCell(cc, d_numMainRows, 3));
		progressPanel.add(new GraphLine(cellSize, 2, SwingConstants.EAST), centerCell(cc, d_numMainRows, 4));
		//NOTE: it is a mystery why numMainRows - 1 is the correct count instead of just numMainRows
		progressPanel.add(new GraphBar(new Dimension(d_barWidth, (int)progressPanel.getPreferredSize().getHeight())), centerCell(cc, d_numMainRows - 1, 5));
		progressPanel.add(new GraphBar(new Dimension(d_barWidth, (int)progressPanel.getPreferredSize().getHeight())), centerCell(cc, d_numMainRows - 1, 12));
		Task assessConvergence = d_model.getModel().getActivityTask().getModel().getStateByName(MCMCModel.CALCULATING_SUMMARIES_PHASE);
		progressPanel.add(new GraphLine(cellSize, 2, SwingConstants.EAST), centerCell(cc, d_numMainRows, 13));
		progressPanel.add(new GraphProgressNode(d_gridCellSize, assessConvergence, false), centerCell(cc, d_numMainRows, 14));
		progressPanel.add(new GraphLine(new Dimension(d_arrowSize, 50), 2, SwingConstants.SOUTH), cc.xywh(14, d_numMainRows / 2 + 2, 1,  d_numMainRows / 2 + 1, CellConstraints.CENTER, CellConstraints.BOTTOM));
		progressPanel.add(new GraphSimpleNode(circleSize, GraphSimpleNodeType.DECISION), cc.xywh(14, d_numMainRows + 2, 1, 1, CellConstraints.CENTER, CellConstraints.CENTER));
		progressPanel.add(new GraphLine(new Dimension((int) (d_edgeLength + (d_edgeLength)), d_arrowSize), 2, SwingConstants.EAST), cc.xyw(14, d_numMainRows + 2, 2, CellConstraints.RIGHT, CellConstraints.DEFAULT));
		progressPanel.add(new GraphSimpleNode(circleSize, GraphSimpleNodeType.END), cc.xy(16, d_numMainRows + 2));
		progressPanel.add(new GraphLine(new Dimension(d_edgeLength * 9, d_arrowSize), 2, SwingConstants.WEST), cc.xyw(10, d_numMainRows + 2, 14 - 7, CellConstraints.LEFT, CellConstraints.DEFAULT));
		progressPanel.add(new GraphBar(new Dimension(d_edgeLength * 2, d_barWidth)), cc.xy(9, d_numMainRows + 2));

		int totalHeight = (int)progressPanel.getPreferredSize().getHeight();
		progressPanel.add(new GraphConnector(new Dimension(d_edgeLength * 2, totalHeight), d_cellHeight + Sizes.DLUY3.getPixelSize(progressPanel), totalHeight - 30, d_numberOfChains), cc.xywh(9, 1, 1, d_numTotalRows));
		
		PanelBuilder builder = new PanelBuilder(new FormLayout("pref", "p"));
		builder.setDefaultDialogBorder();
		builder.add(progressPanel);
		return builder.getPanel();
	}
	
	private static String createFormSpec(String rowSpec, final int numRows) {
		String[] rowArray = new String[numRows];
		Arrays.fill(rowArray, rowSpec);
		String completeRowSpec = StringUtils.join(rowArray, ",");
		return completeRowSpec;
	}

	private static CellConstraints centerCell(CellConstraints cc, int rowSpan, int col) {
		return cc.xywh(col, 1, 1, rowSpan, CellConstraints.CENTER, CellConstraints.CENTER);
	}
	
}
