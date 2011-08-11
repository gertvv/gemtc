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

import com.jgoodies.binding.value.ValueHolder;
import java.beans.PropertyChangeListener;

import org.junit.Test;
import static org.junit.Assert.*;
import org.drugis.common.JUnitUtil;
import static org.easymock.EasyMock.*;

public class StringNotEmptyModelTest {
	@Test
	public void testInitialValues() {
		assertFalse(new StringNotEmptyModel(new ValueHolder(null)).getValue());
		assertFalse(new StringNotEmptyModel(new ValueHolder(new Object())).getValue());
		assertFalse(new StringNotEmptyModel(new ValueHolder("")).getValue());
		assertTrue(new StringNotEmptyModel(new ValueHolder("Test")).getValue());
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testSetValueNotSupported() {
		new StringNotEmptyModel(new ValueHolder(null)).setValue(true);
	}

	@Test
	public void testEventChaining() {
		ValueHolder holder = new ValueHolder(null);
		StringNotEmptyModel model = new StringNotEmptyModel(holder);

		PropertyChangeListener mock = JUnitUtil.mockStrictListener(model, "value", false, true);
		model.addValueChangeListener(mock);

		holder.setValue("test");
		holder.setValue("test2");
		verify(mock);

		model.removeValueChangeListener(mock);

		mock = JUnitUtil.mockStrictListener(model, "value", true, false);
		model.addValueChangeListener(mock);

		holder.setValue("");
		verify(mock);
	}
}
