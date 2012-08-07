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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.SwingConstants;

public class GraphSimpleNode extends GraphComponent implements SwingConstants {
	private static final long serialVersionUID = 7712060678575254850L;
	
	public enum GraphSimpleNodeType {
		START, END, DECISION
	}

	private GraphSimpleNodeType d_type;
	
	public GraphSimpleNode(Dimension gridCellSize, GraphSimpleNodeType type) {
		this(gridCellSize, DEFAULT_LINE_WIDTH, DEFAULT_COLOR, type);
	}
	
	public GraphSimpleNode(Dimension gridCellSize, int lineWidth, Color color, GraphSimpleNodeType type) {
		super(gridCellSize, lineWidth, color);		
		d_type = type;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;  
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		double w = getPreferredSize().getWidth();
		double h = getPreferredSize().getHeight();
		
		g2.setColor(d_color);
		g2.setStroke(new BasicStroke((float)d_lineWidth));
		
		switch (d_type) {
		case START:
			g2.fillOval(0, 0, (int)w, (int)h);
			break;
		case END:
			double factor = 0.6;
			int baseDiameter = (int)(w - d_lineWidth * 2);
			int scaledDiameter = (int)(baseDiameter * factor);
			
			g2.translate(baseDiameter / 2 + d_lineWidth / 2, baseDiameter / 2 + d_lineWidth / 2);
			g2.fillOval(-scaledDiameter / 2, -scaledDiameter / 2, scaledDiameter, scaledDiameter);
			g2.drawOval(-baseDiameter / 2, -baseDiameter / 2, baseDiameter, baseDiameter);
			break;
		case DECISION: // NOTE: you could use the createDiamond from ShapeUtilities
			int sideLength = (int)Math.sqrt(0.5 * w * w);
			int offset = (int)((w - sideLength) / 2);
			g2.rotate(Math.PI / 4, w / 2, h / 2);
			g2.drawRect(offset, offset, sideLength, sideLength);
			break;
		}
	}
}

