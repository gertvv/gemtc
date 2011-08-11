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

import com.jgoodies.binding.list.ObservableList;
import com.jgoodies.binding.beans.Observable;

import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Ensures that a PropertyChangeListener is attached to each of the Observables in an ObservableList.
 */
public class ListPropertyChangeProxy<T extends Observable> {
	private ObservableList<T> d_list;
	private PropertyChangeListener d_listener;
	private List<T> d_copy = new ArrayList<T>();

	public ListPropertyChangeProxy(ObservableList<T> list, PropertyChangeListener listener) {
		d_list = list;
		d_listener = listener;

		d_list.addListDataListener(new ListDataListener() {
			public void intervalRemoved(ListDataEvent e) {
				removed(e.getIndex0(), e.getIndex1());
			}
			public void intervalAdded(ListDataEvent e) {
				added(e.getIndex0(), e.getIndex1());
			}
			public void contentsChanged(ListDataEvent e) {
				changed(e.getIndex0(), e.getIndex1());
			}
		});
		added(0, d_list.size() - 1);
	}

	public void added(int start, int end) {
		for (int i = start; i <= end; ++i) {
			d_list.get(i).addPropertyChangeListener(d_listener);
			d_copy.add(i, d_list.get(i));
		}
	}

	public void removed(int start, int end) {
		for (int i = end; i >= start; ++i) {
			d_copy.get(i).removePropertyChangeListener(d_listener);
			d_copy.remove(i);
		}
	}

	public void changed(int start, int end) {
		for (int i = start; i <= end; ++i) {
			d_copy.get(i).removePropertyChangeListener(d_listener);
			d_copy.set(i, d_list.get(i));
			d_list.get(i).addPropertyChangeListener(d_listener);
		}
	}
}
