/**
 * 
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
