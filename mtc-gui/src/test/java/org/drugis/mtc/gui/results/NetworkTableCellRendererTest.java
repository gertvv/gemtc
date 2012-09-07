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

package org.drugis.mtc.gui.results;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections15.Transformer;
import org.drugis.mtc.ConsistencyModel;
import org.drugis.mtc.DefaultModelFactory;
import org.drugis.mtc.DichotomousNetworkBuilder;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.presentation.SimulationConsistencyWrapper;
import org.drugis.mtc.presentation.results.NetworkRelativeEffectTableModel;
import org.junit.Before;
import org.junit.Test;

public class NetworkTableCellRendererTest {
	private NetworkRelativeEffectTableModel d_tableModel;
	private SimulationConsistencyWrapper<String> d_consistencyWrapper;
	private List<Treatment> d_treatments;

	@Before
	public void setUp() {
		List<String> alternatives = Arrays.asList("Fluoxetine", "Paroxetine", "Sertraline");
		DichotomousNetworkBuilder<String> builder = new DichotomousNetworkBuilder<String>(
				new Transformer<String,String>() {
					@Override
					public String transform(String input) {
						return input.substring(0, 1);
					}
				},
				new Transformer<String,String>() {
					@Override
					public String transform(String input) {
						return input;
					}
				});
		builder.add("Bennie", "Fluoxetine", 1, 100);
		builder.add("Bennie", "Sertraline", 1, 100);
		builder.add("Chouinard", "Fluoxetine", 1, 100);
		builder.add("Chouinard", "Paroxetine", 1, 100);
		builder.add("De Wilde", "Paroxetine", 1, 100);
		builder.add("De Wilde", "Fluoxetine", 1, 100);
		builder.add("Fava", "Sertraline", 1, 100);
		builder.add("Fava", "Fluoxetine", 1, 100);
		builder.add("Fava", "Paroxetine", 1, 100);
		Network network = builder.buildNetwork();
		
		final ConsistencyModel model = DefaultModelFactory.instance().getConsistencyModel(network);
		d_consistencyWrapper = new SimulationConsistencyWrapper<String>(model, alternatives, builder.getTreatmentMap());
		d_tableModel = new NetworkRelativeEffectTableModel(network.getTreatments(), d_consistencyWrapper);
		d_treatments = network.getTreatments();
	}
	
	@Test
	public void testText() {
		NetworkRelativeEffectTableCellRenderer renderer1 = new NetworkRelativeEffectTableCellRenderer(false, true);
		assertEquals("Fluoxetine", renderer1.getText(d_treatments.get(0)));
		assertEquals("Paroxetine", renderer1.getText(d_treatments.get(1)));
		assertEquals("Sertraline", renderer1.getText(d_treatments.get(2)));
		NetworkRelativeEffectTableCellRenderer renderer2 = new NetworkRelativeEffectTableCellRenderer(false, false);
		assertEquals("F", renderer2.getText(d_treatments.get(0)));
		assertEquals("P", renderer2.getText(d_treatments.get(1)));
		assertEquals("S", renderer2.getText(d_treatments.get(2)));
	}
	
	public void testTooltip() {
		NetworkRelativeEffectTableCellRenderer renderer1 = new NetworkRelativeEffectTableCellRenderer(false, true);
		assertEquals("Fluoxetine", renderer1.getTooltipAt(d_tableModel, 0, 0));
		assertEquals("Paroxetine", renderer1.getTooltipAt(d_tableModel, 1, 1));
		assertEquals("\"Paroxetine\" relative to \"Fluoxetine\"", renderer1.getTooltipAt(d_tableModel, 0, 1));
		assertEquals("\"Fluoxetine\" relative to \"Paroxetine\"", renderer1.getTooltipAt(d_tableModel, 1, 0));
		assertEquals("\"Fluoxetine\" relative to \"Sertraline\"", renderer1.getTooltipAt(d_tableModel, 2, 0));
		NetworkRelativeEffectTableCellRenderer renderer2 = new NetworkRelativeEffectTableCellRenderer(false, false);
		assertEquals("F", renderer2.getTooltipAt(d_tableModel, 0, 0));
		assertEquals("\"P\" relative to \"F\"", renderer2.getTooltipAt(d_tableModel, 0, 1));
	}
}
