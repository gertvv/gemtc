/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
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

package org.drugis.mtc.gui;

import com.jgoodies.binding.list.ArrayListModel;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import org.junit.Test;
import static org.junit.Assert.*;
import org.drugis.common.JUnitUtil;
import static org.easymock.EasyMock.*;

public class ListMinimumSizeModelTest {
	@Test
	public void testInitialValues() {
		assertFalse(new ListMinimumSizeModel(new ArrayListModel<String>(), 1).getValue());
		assertTrue(new ListMinimumSizeModel(new ArrayListModel<String>(Arrays.asList("Test")), 1).getValue());
		assertFalse(new ListMinimumSizeModel(new ArrayListModel<String>(Arrays.asList("Test")), 2).getValue());
		assertTrue(new ListMinimumSizeModel(new ArrayListModel<String>(Arrays.asList("Test", "Test")), 2).getValue());
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testSetValueNotSupported() {
		new ListMinimumSizeModel(new ArrayListModel<String>(), 2).setValue(true);
	}

	@Test
	public void testEventChaining() {
		ArrayListModel<String> list = new ArrayListModel<String>(Arrays.asList("Test"));
		ListMinimumSizeModel model = new ListMinimumSizeModel(list, 2);

		PropertyChangeListener mock = JUnitUtil.mockStrictListener(model, "value", false, true);
		model.addValueChangeListener(mock);

		list.add("2");
		list.add("3");
		list.remove("3");
		verify(mock);

		model.removeValueChangeListener(mock);

		mock = JUnitUtil.mockStrictListener(model, "value", true, false);
		model.addValueChangeListener(mock);

		list.remove("Test");
		verify(mock);
	}
}

