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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.SwingConstants;

import org.drugis.common.gui.task.TaskProgressBar;
import org.drugis.common.gui.task.TaskProgressModel;
import org.drugis.common.threading.Task;
import org.drugis.common.threading.TaskListener;
import org.drugis.common.threading.event.TaskEvent;
import org.drugis.common.threading.event.TaskEvent.EventType;

public class GraphProgressNode extends GraphComponent implements SwingConstants {
	private static final Color DARK_RED = Color.decode("#C4000D");
	private static final Color SKY_BLUE = Color.decode("#7DA6FF");
	private static final Color GREEN = Color.decode("#D7FF96");

	private static final long serialVersionUID = 7151331776919970759L;

	private static final double DEFAULT_ROUNDING_ARCH = 5;

	private String d_labelText;
	private final boolean d_hasProgress;

	private Color d_color = Color.LIGHT_GRAY;
	
	public GraphProgressNode(Dimension gridCellSize, Task task) {
		this(gridCellSize, DEFAULT_LINE_WIDTH, DEFAULT_COLOR, task);
	}
	
	public GraphProgressNode(Dimension gridCellSize, int lineWidth, Color color, Task task) {
		this(gridCellSize, lineWidth, color, task, true);
	}
	
	
	public GraphProgressNode(Dimension gridCellSize, Task task, boolean hasProgress) {
		this(gridCellSize, DEFAULT_LINE_WIDTH, DEFAULT_COLOR, task, hasProgress);
	}
	
	
	public GraphProgressNode(Dimension gridCellSize, int lineWidth, Color color, Task task, boolean hasProgress) {
		super(gridCellSize, lineWidth, color);
		d_labelText = task.toString();
		d_hasProgress = hasProgress;
		if(hasProgress) { 
			TaskProgressBar tpb = new TaskProgressBar(new TaskProgressModel(task));			
			setLayout(new BorderLayout(0, 0));
			add(tpb, BorderLayout.NORTH);
			tpb.setVisible(true);
			revalidate();
		}
		setNodeColors(task);
		task.addTaskListener(new TaskListener() {
			
			@Override
			public void taskEvent(TaskEvent event) {
				if(event.getType() == EventType.TASK_FINISHED) {
					d_color = GREEN;
				} else if(event.getType() == EventType.TASK_STARTED) {
					d_color = SKY_BLUE;
				} else if(event.getType() == EventType.TASK_FAILED || event.getType() == EventType.TASK_ABORTED) {
					d_color = DARK_RED;
				} else if(event.getType() == EventType.TASK_RESTARTED) {
					d_color = Color.LIGHT_GRAY;
				}
				GraphProgressNode.this.repaint();
			}
		});
	}

	private void setNodeColors(Task task) {
		if(task.isAborted() || task.isFailed()) { 
			d_color = DARK_RED;
		} else if(task.isStarted() && !task.isFinished()) {
			d_color = SKY_BLUE;
		} else if (task.isFinished()) {
			d_color = GREEN;
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		if(!d_hasProgress) {
			Graphics2D g2 = (Graphics2D)g;  
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			Dimension s = getPreferredSize();
			g2.setStroke(new BasicStroke((float)d_lineWidth));
			RoundRectangle2D.Double s2 = new RoundRectangle2D.Double(d_lineWidth / 2.0, d_lineWidth / 2.0, s.getWidth() - d_lineWidth, s.getHeight() - d_lineWidth, DEFAULT_ROUNDING_ARCH, DEFAULT_ROUNDING_ARCH);
			g2.setPaint(d_color);
			g2.fill(s2);
			g2.setPaint(DEFAULT_COLOR);
			g2.draw(s2);

			Rectangle2D textBounds = g2.getFontMetrics().getStringBounds(d_labelText, g);
			g2.drawString(d_labelText, (float)(s.width / 2 - textBounds.getCenterX()), (float)(s.height / 2 - textBounds.getCenterY()));
		}
	}
}

