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
import java.awt.geom.Line2D;

import javax.swing.SwingConstants;

public class GraphLine extends GraphComponent implements SwingConstants {
	private static final long serialVersionUID = 7151331776919970759L;

	private static final int DEFAULT_DIRECTION = EAST;
	private static final int _DEFAULT_BARB_LENGTH = 25;
	private static final double DEFAULT_BARB_ANGLE = Math.PI / 6.0;
	
	private int d_direction;
	private int d_barbLength;
	private double d_barbAngle;

	private final boolean d_hasArrow;
	
	public GraphLine(Dimension gridCellSize) {
		this(gridCellSize, DEFAULT_LINE_WIDTH, DEFAULT_COLOR, DEFAULT_DIRECTION, true);
	}
	
	public GraphLine(Dimension gridCellSize, int lineWidth, int direction) {
		this(gridCellSize, lineWidth, DEFAULT_COLOR, direction, true);
	}
	
	public GraphLine(Dimension gridCellSize, int lineWidth, int direction, boolean hasArrow) {
		this(gridCellSize, lineWidth, DEFAULT_COLOR, direction, hasArrow);
	}
	
	public GraphLine(Dimension gridCellSize, int lineWidth, Color color, int direction, boolean hasArrow) {
		super(gridCellSize, lineWidth, color);
		d_hasArrow = hasArrow;
		
		if (direction!= NORTH && direction != EAST && direction != SOUTH && direction != WEST) {
			throw new IllegalArgumentException(direction + " is not a legal direction");
		}
		
		d_direction = direction;
		d_barbLength = _DEFAULT_BARB_LENGTH;
		d_barbAngle = DEFAULT_BARB_ANGLE;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;  
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		Dimension s = getSize();
		
		g2.setColor(d_color);
		g2.setStroke(new BasicStroke((int)d_lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		
		if (d_direction == WEST || d_direction == EAST) {
			double y = s.height / 2;
			g2.draw(new Line2D.Double(0, y, s.width, y));
			if(d_hasArrow) {
				if (d_direction == EAST) {
					drawArrow(g2, 0.0, s.width, y);
				} else if (d_direction == WEST) {
					drawArrow(g2, Math.PI, 0.0, y);
				}
			}	

		} else if (d_direction == NORTH || d_direction == SOUTH) {
			double x = s.width / 2;
			g2.draw(new Line2D.Double(x, 0, x, s.height));
			if(d_hasArrow) {
				if (d_direction == NORTH) {
					drawArrow(g2, Math.PI * 1.5, x, 0);
				}
				else if (d_direction == SOUTH) {
					drawArrow(g2, Math.PI / 2, x, s.height);
				}
			}
		}
	}
	
	//NOTE: modified from http://www.coderanch.com/t/339505/GUI/java/drawing-arrows
	private void drawArrow(Graphics2D g2, double theta, double x0, double y0) {
		double x = x0 - d_barbLength * Math.cos(theta + d_barbAngle);
        double y = y0 - d_barbLength * Math.sin(theta + d_barbAngle);
        g2.draw(new Line2D.Double(x0, y0, x, y));
        x = x0 - d_barbLength * Math.cos(theta - d_barbAngle);
        y = y0 - d_barbLength * Math.sin(theta - d_barbAngle);
        g2.draw(new Line2D.Double(x0, y0, x, y));
	}
}

