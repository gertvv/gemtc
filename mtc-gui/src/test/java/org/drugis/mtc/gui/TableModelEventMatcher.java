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

