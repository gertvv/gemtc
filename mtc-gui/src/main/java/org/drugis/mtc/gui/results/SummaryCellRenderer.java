/*
 * This file is part of ADDIS (Aggregate Data Drug Information System).
 * ADDIS is distributed from http://drugis.org/.
 * Copyright (C) 2009 Gert van Valkenhoef, Tommi Tervonen.
 * Copyright (C) 2010 Gert van Valkenhoef, Tommi Tervonen, 
 * Tijs Zwinkels, Maarten Jacobs, Hanno Koeslag, Florin Schimbinschi, 
 * Ahmad Kamal, Daniel Reid.
 * Copyright (C) 2011 Gert van Valkenhoef, Ahmad Kamal, 
 * Daniel Reid, Florin Schimbinschi.
 * Copyright (C) 2012 Gert van Valkenhoef, Daniel Reid, 
 * Joël Kuiper, Wouter Reckman.
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

package org.drugis.mtc.gui.results;

import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.drugis.mtc.summary.NodeSplitPValueSummary;
import org.drugis.mtc.summary.NormalSummary;
import org.drugis.mtc.summary.QuantileSummary;

/**
 * Render instances of org.drugis.mtc.summary.Summary. Can optionally render the exponent of the quantities.
 */
public class SummaryCellRenderer implements TableCellRenderer {
	private static final DecimalFormat s_format = new DecimalFormat("0.00");
	private final boolean d_applyExpTransform;
	
	/**
	 * Render summaries (without applying a transformation).
	 */
	public SummaryCellRenderer() {
		this(false);
	}

	/**
	 * Render summaries, optionally applying an exponential transformation.
	 * @param applyExpTransform If true, will render e^x instead of x.
	 */
	public SummaryCellRenderer(boolean applyExpTransform) {
		d_applyExpTransform = applyExpTransform;
	}

	public Component getTableCellRendererComponent(JTable table, Object cellContents,
			boolean isSelected, boolean hasFocus, int row, int column) {
		return (new DefaultTableCellRenderer()).getTableCellRendererComponent(table, getCellText(cellContents), isSelected, hasFocus, row, column);
	}

	private String getCellText(Object cellContents) {
		String str = "N/A";
		if (cellContents instanceof NormalSummary) {
			str = getNormalSummaryString(cellContents);
		} else if (cellContents instanceof QuantileSummary) {
			str = getQuantileSummaryString(cellContents);
		} else if (cellContents instanceof NodeSplitPValueSummary) {
			str = getNodeSplitPvalueString(cellContents);
		} else if (cellContents instanceof Number) {
			str = getDoubleString(cellContents);
		}
		return str;
	}

	private String getDoubleString(Object cellContents) {
		return format((Double)cellContents);
	}

	private String getNodeSplitPvalueString(Object cellContents) {
		NodeSplitPValueSummary re = (NodeSplitPValueSummary)cellContents;
		
		String str = "N/A";
		if (re != null && re.getDefined()) {
			str = format(re.getPvalue());
		}
		return str;
	}

	private String getQuantileSummaryString(Object cellContents) {
		QuantileSummary re = (QuantileSummary)cellContents;
		
		String str = "N/A";
		if (re != null && re.getDefined()) {
			str = formatQuantile(re, 0.5) + " (" + 
				formatQuantile(re, 0.025) + ", " + formatQuantile(re, 0.975) + ")";
		}
		
		return str;
	}

	private String formatQuantile(QuantileSummary re, double p) {
		if (d_applyExpTransform) { 
			return format(Math.exp(re.getQuantile(re.indexOf(p))));
		} else {
			return format(re.getQuantile(re.indexOf(p)));
		}
	}
	
	private String getNormalSummaryString(Object cellContents) {
		NormalSummary re = (NormalSummary)cellContents;
		
		String str = "N/A";
		if (re != null && re.getDefined()) {
			String mu = format(re.getMean());
			String sigma = format(re.getStandardDeviation());
			if (d_applyExpTransform) { 
				str = "LogNormal(" + mu + ", " + sigma + ")";
			} else { 
				str = mu + " \u00B1 " + sigma;
			}
		}
		return str;
	}
	
	public static String format(double d) {
    	return s_format.format(d);
	 }
}