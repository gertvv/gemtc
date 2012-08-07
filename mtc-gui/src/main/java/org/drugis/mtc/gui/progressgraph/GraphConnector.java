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

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.SwingConstants;

public class GraphConnector extends GraphComponent implements SwingConstants {
	private static final long serialVersionUID = 3271331606688687191L;
	private final int d_numberOfChains;
	private final int d_cellHeight;
	private final int d_totalHeight;

	public GraphConnector(Dimension gridCellSize, int cellHeight, int totalHeight, int numberOfChains) {
		super(gridCellSize);
		d_cellHeight = cellHeight;
		d_totalHeight = totalHeight;
		d_numberOfChains = numberOfChains;
	}


	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;  
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(d_color);
		g2.setStroke(new BasicStroke((int)d_lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		double w = getSize().getWidth();
//		double h = getSize().getHeight();
		double step = w / (d_numberOfChains + 1);
		
		for (int i = 0; i < d_numberOfChains; ++i) {
			int hch = d_cellHeight / 2;
			double x = step + step * i;
			double y = hch + d_cellHeight * i;
			
			g2.drawLine((int)x, (int)y, (int)x, d_totalHeight);
		}
	}

}

