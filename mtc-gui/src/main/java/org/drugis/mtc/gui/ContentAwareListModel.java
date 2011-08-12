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

import javax.swing.ListModel;
import javax.swing.AbstractListModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import java.util.Arrays;
import java.util.List;

/**
 * ListModel that wraps an ObservableList. Will fire events when the ObservableList does.
 * In addition, it will fireContentsChanged when certain properties of the contained Observables change.
 */
public class ContentAwareListModel<T extends Observable> extends AbstractListModel {
	private ObservableList d_nested;
	private ListPropertyChangeProxy<T> d_proxy;
	private List<String> d_properties;

	public ContentAwareListModel(ObservableList<T> list, String[] properties) {
		d_nested = list;
		d_properties = Arrays.asList(properties);

		d_nested.addListDataListener(new ListDataListener() {
			public void intervalRemoved(ListDataEvent e) {
				System.out.println("Interval removed " + e.getIndex0() + "-" + e.getIndex1());
				fireIntervalRemoved(ContentAwareListModel.this, e.getIndex0(), e.getIndex1());
			}
			public void intervalAdded(ListDataEvent e) {
				System.out.println("Interval added " + e.getIndex0() + "-" + e.getIndex1());
				fireIntervalAdded(ContentAwareListModel.this, e.getIndex0(), e.getIndex1());
			}
			public void contentsChanged(ListDataEvent e) {
				System.out.println("Interval changed " + e.getIndex0() + "-" + e.getIndex1());
				fireContentsChanged(ContentAwareListModel.this, e.getIndex0(), e.getIndex1());
			}
		});

		d_proxy = new ListPropertyChangeProxy(d_nested, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (d_properties.contains(evt.getPropertyName())) {
					int idx = d_nested.indexOf(evt.getSource());
					fireContentsChanged(ContentAwareListModel.this, idx, idx);
				}
			}
		});
	}

	public Object getElementAt(int idx) {
		return d_nested.getElementAt(idx);
	}

	public int getSize() {
		return d_nested.getSize();
	}
}
