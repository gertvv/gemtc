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

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JComponent;

public abstract class GraphComponent extends JComponent {
	private static final long serialVersionUID = -2929257889835014286L;
	
	protected static final int DEFAULT_LINE_WIDTH = 2;
	protected static final Color DEFAULT_COLOR = Color.BLACK;
	
	protected int d_lineWidth;
	protected Color d_color;

	public GraphComponent(Dimension gridCellSize) {
		this(gridCellSize, DEFAULT_LINE_WIDTH, DEFAULT_COLOR);
	}
	
	public GraphComponent(Dimension gridCellSize, int lineWidth, Color color) {
		super();
		
		d_color = color;
		d_lineWidth = lineWidth;
		
		setPreferredSize(gridCellSize);
		setMinimumSize(gridCellSize);
		setMaximumSize(gridCellSize);
		revalidate();
	}
}