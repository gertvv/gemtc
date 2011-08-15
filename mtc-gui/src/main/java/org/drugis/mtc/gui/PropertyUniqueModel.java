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

import java.util.List;
import java.beans.PropertyDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import com.jgoodies.binding.BindingUtils;
import com.jgoodies.binding.beans.Observable;
import com.jgoodies.binding.beans.BeanUtils;

import com.jgoodies.binding.value.AbstractValueModel;

/**
 * Listens to an item's property and checks whether it's value is unique within the given list.
 * The list is assumed to be static for the lifetime of this model.
 */
public class PropertyUniqueModel<T extends Observable> extends AbstractValueModel {
	private List<T> d_list;
	private T d_item;
	private PropertyDescriptor d_property;
	private boolean d_val;

	public PropertyUniqueModel(List<T> list, T item, final String propertyName) {
		d_list = list;
		d_item = item;
		try {
			d_property = BeanUtils.getPropertyDescriptor(item.getClass(), propertyName);
		} catch (java.beans.IntrospectionException e) {
			throw new RuntimeException(e);
		}
		d_item.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(propertyName)) {
					update();
				}
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
		for (T x : d_list) {
			if (x != d_item) {
				if (BindingUtils.equals(BeanUtils.getValue(x, d_property), BeanUtils.getValue(d_item, d_property))) {
					return false;
				}
			}
		}
		return true;
	}
}


