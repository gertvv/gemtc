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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.drugis.common.JUnitUtil;
import org.drugis.mtc.gui.results.SummaryCellRenderer;
import org.drugis.mtc.summary.NodeSplitPValueSummary;
import org.drugis.mtc.summary.NormalSummary;
import org.drugis.mtc.summary.QuantileSummary;
import org.drugis.mtc.test.ExampleResults;
import org.junit.Test;

public class SummaryCellRendererTest {
	@Test
	public void testGetTableCellRendererComponent() throws IOException {
		SummaryCellRenderer summaryCellRenderer = new SummaryCellRenderer();
		DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();
		ExampleResults results = new ExampleResults();
		
		assertEquals(
				defaultRenderer.getTableCellRendererComponent(new JTable(), "N/A", true, true, 1, 1).toString(),
				summaryCellRenderer.getTableCellRendererComponent(new JTable(), "Test", true, true, 1, 1).toString()
				);
		
		JUnitUtil.assertNotEquals(
				defaultRenderer.getTableCellRendererComponent(new JTable(), "N/A", true, true, 1, 1).toString(),
				summaryCellRenderer.getTableCellRendererComponent(new JTable(), "Test", false, false, 2, 3).toString()
				);
		
		assertEquals(
				defaultRenderer.getTableCellRendererComponent(new JTable(), "N/A" , true, true, 1, 1).toString(),
				summaryCellRenderer.getTableCellRendererComponent(new JTable(), new NormalSummary(results, results.getParameters()[0]), true, true, 1, 1).toString()
				);
		
		assertEquals(
				defaultRenderer.getTableCellRendererComponent(new JTable(), "N/A" , true, true, 1, 1).toString(),
				summaryCellRenderer.getTableCellRendererComponent(new JTable(), new QuantileSummary(results, results.getParameters()[0]), true, true, 2, 2).toString()
				);
		
		assertEquals(
				defaultRenderer.getTableCellRendererComponent(new JTable(), "N/A" , true, true, 1, 1).toString(),
				summaryCellRenderer.getTableCellRendererComponent(new JTable(), new NodeSplitPValueSummary(results, results.getParameters()[0], results.getParameters()[1]), true, true, 3, 3).toString()
				);

		results.makeSamplesAvailable();
		
		assertEquals(
				defaultRenderer.getTableCellRendererComponent(new JTable(), "1.34 \u00B1 0.29" , true, true, 1, 1).toString(),
				summaryCellRenderer.getTableCellRendererComponent(new JTable(), new NormalSummary(results, results.getParameters()[0]), true, true, 1, 1).toString()
				);
		
		assertEquals(
				defaultRenderer.getTableCellRendererComponent(new JTable(), "1.35 (0.74, 1.87)" , true, true, 1, 1).toString(),
				summaryCellRenderer.getTableCellRendererComponent(new JTable(), new QuantileSummary(results, results.getParameters()[0]), true, true, 2, 2).toString()
				);
		
		assertEquals(
				defaultRenderer.getTableCellRendererComponent(new JTable(), "0.05" , true, true, 1, 1).toString(),
				summaryCellRenderer.getTableCellRendererComponent(new JTable(), new NodeSplitPValueSummary(results, results.getParameters()[0], results.getParameters()[1]), true, true, 3, 3).toString()
				);
	}
	
	@Test
	public void testRenderWithExpTransform() throws IOException {
		SummaryCellRenderer summaryCellRenderer = new SummaryCellRenderer(true);
		DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();
		ExampleResults results = new ExampleResults();
		results.makeSamplesAvailable();

		assertEquals(
				defaultRenderer.getTableCellRendererComponent(new JTable(), "3.88 (2.10, 6.50)" , true, true, 1, 1).toString(),
				summaryCellRenderer.getTableCellRendererComponent(new JTable(), new QuantileSummary(results, results.getParameters()[0]), true, true, 2, 2).toString()
				);
		
		assertEquals(
				defaultRenderer.getTableCellRendererComponent(new JTable(), "LogNormal(1.34, 0.29)" , true, true, 1, 1).toString(),
				summaryCellRenderer.getTableCellRendererComponent(new JTable(), new NormalSummary(results, results.getParameters()[0]), true, true, 1, 1).toString()
				);
		
		assertEquals(
				defaultRenderer.getTableCellRendererComponent(new JTable(), "0.05" , true, true, 1, 1).toString(),
				summaryCellRenderer.getTableCellRendererComponent(new JTable(), new NodeSplitPValueSummary(results, results.getParameters()[0], results.getParameters()[1]), true, true, 3, 3).toString()
				);
	}
}