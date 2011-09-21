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

import static org.easymock.EasyMock.reportMatcher;

import javax.swing.event.ListDataEvent;

import org.easymock.IArgumentMatcher;

public class ListDataEventMatcher implements IArgumentMatcher {
	private ListDataEvent d_expected;

	public ListDataEventMatcher(ListDataEvent expected) {
		d_expected = expected;
	}

	public void appendTo(StringBuffer buffer) {
		buffer.append("ListDataEventMatcher(");
		buffer.append("source = " + d_expected.getSource() + ", ");
		buffer.append("type = " + d_expected.getType() + ", ");
		buffer.append("index0 = " + d_expected.getIndex0() + ", ");
		buffer.append("index1 = " + d_expected.getIndex1() + ")");
	}

	public boolean matches(Object a) {
		if (!(a instanceof ListDataEvent)) {
			return false;
		}
		ListDataEvent actual = (ListDataEvent)a;
		return actual.getSource() == d_expected.getSource() &&
		actual.getType() == d_expected.getType() &&
		actual.getIndex0() == d_expected.getIndex0() &&
		actual.getIndex1() == d_expected.getIndex1();
	}

	public static ListDataEvent eqListDataEvent(ListDataEvent in) {
	    reportMatcher(new ListDataEventMatcher(in));
	    return null;
	}
}
