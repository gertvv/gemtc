/*
 * This file is part of ADDIS (Aggregate Data Drug Information System).
 * ADDIS is distributed from http://drugis.org/.
 * Copyright © 2009 Gert van Valkenhoef, Tommi Tervonen.
 * Copyright © 2010 Gert van Valkenhoef, Tommi Tervonen, Tijs Zwinkels,
 * Maarten Jacobs, Hanno Koeslag, Florin Schimbinschi, Ahmad Kamal, Daniel
 * Reid.
 * Copyright © 2011 Gert van Valkenhoef, Ahmad Kamal, Daniel Reid, Florin
 * Schimbinschi.
 * Copyright © 2012 Gert van Valkenhoef, Daniel Reid, Joël Kuiper, Wouter
 * Reckman.
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

package org.drugis.mtc.util;

import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.beans.PropertyChangeListener;

import org.drugis.common.JUnitUtil;
import org.drugis.mtc.util.MCMCResultsAvailableModel;
import org.junit.Test;

public class MCMCResultsAvailableModelTest {

	@Test
	public void testInitalisation() {
		FakeResults res = new FakeResults(3, 50, 2, false);
		assertEquals(false, new MCMCResultsAvailableModel(res).getValue());
		res.makeResultsAvailable();
		assertEquals(true, new MCMCResultsAvailableModel(res).getValue());
	}
	
	@Test
	public void testDynamicValue() {
		FakeResults res = new FakeResults(3, 50, 2, false);
		MCMCResultsAvailableModel model = new MCMCResultsAvailableModel(res);
		res.makeResultsAvailable();
		assertEquals(true, model.getValue());
		res.clear();
		assertEquals(false, model.getValue());
	}

	@Test
	public void testFireValueChange() {
		FakeResults res = new FakeResults(3, 50, 2, false);
		MCMCResultsAvailableModel model = new MCMCResultsAvailableModel(res);
		PropertyChangeListener mock = JUnitUtil.mockListener(model, "value", false, true);
		model.addValueChangeListener(mock);
		
		res.makeResultsAvailable();
		verify(mock);
	}
}
