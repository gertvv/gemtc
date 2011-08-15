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

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;

import com.jgoodies.binding.value.AbstractValueModel;

/**
 * Listens to a ListModel, and converts it to true iff list.getSize() &gt;= minSize.
 */
public class ListMinimumSizeModel extends AbstractValueModel {
	private ListModel d_list;
	private int d_minSize;
	private boolean d_val;

	public ListMinimumSizeModel(ListModel list, int minSize) {
		d_list = list;
		d_minSize = minSize;
		d_list.addListDataListener(new ListDataListener() {
			public void contentsChanged(ListDataEvent e) {
				update();
			}
			public void intervalAdded(ListDataEvent e) {
				update();
			}
			public void intervalRemoved(ListDataEvent e) {
				update();
			}
		});
		d_val = calc();
	}

	public Boolean getValue() {
		return d_val;
	}

	public void setValue(Object value) {
		throw new UnsupportedOperationException();
	}

	private void update() {
		boolean oldVal = d_val;
		d_val = calc();
		fireValueChange(oldVal, d_val);
	}

	private boolean calc() {
		return d_list.getSize() >= d_minSize;
	}
}

