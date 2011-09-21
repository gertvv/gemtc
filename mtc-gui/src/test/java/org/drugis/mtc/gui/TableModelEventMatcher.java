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

import javax.swing.event.TableModelEvent;

import org.easymock.IArgumentMatcher;

public class TableModelEventMatcher implements IArgumentMatcher {
    private TableModelEvent d_expected;

    public TableModelEventMatcher(TableModelEvent expected) {
        d_expected = expected;
    }

    public void appendTo(StringBuffer buffer) {
        buffer.append("TableModelEventMatcher(");
        buffer.append("source = " + d_expected.getSource() + ", ");
        buffer.append("type = " + d_expected.getType() + ", ");
        buffer.append("firstRow = " + d_expected.getFirstRow() + ", ");
        buffer.append("lastRow = " + d_expected.getLastRow() + ", ");
        buffer.append("column = " + d_expected.getColumn() + ")");
    }

    public boolean matches(Object a) {
        if (!(a instanceof TableModelEvent)) {
            return false;
        }
        TableModelEvent actual = (TableModelEvent)a;
        return actual.getSource() == d_expected.getSource() &&
        actual.getType() == d_expected.getType() &&
        actual.getFirstRow() == d_expected.getFirstRow() &&
        actual.getLastRow() == d_expected.getLastRow() &&
        actual.getColumn() == d_expected.getColumn();
    }

    public static TableModelEvent eqTableModelEvent(TableModelEvent in) {
        reportMatcher(new TableModelEventMatcher(in));
        return null;
    }
}

