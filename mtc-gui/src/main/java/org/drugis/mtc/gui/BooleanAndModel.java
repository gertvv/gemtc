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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.binding.value.AbstractValueModel;

/**
 * Listens to two nested ValueModels, and converts to true iff both are true. Converts to null if either one is null, or not a Boolean.
 */
public class BooleanAndModel extends AbstractValueModel {
	private static final long serialVersionUID = 8591942709442108053L;
	private ValueModel d_bool1;
	private ValueModel d_bool2;
	private Boolean d_val;

	public BooleanAndModel(ValueModel bool1, ValueModel bool2) {
		d_bool1 = bool1;
		d_bool2 = bool2;
		PropertyChangeListener listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				Object oldVal = d_val;
				d_val = calc();
				fireValueChange(oldVal, d_val);
			}
		};
		d_bool1.addValueChangeListener(listener);
		d_bool2.addValueChangeListener(listener);
		d_val = calc();
	}

	public Boolean getValue() {
		return d_val;
	}

	public void setValue(Object value) {
		throw new UnsupportedOperationException();
	}

	private boolean isBoolean(ValueModel model) {
		return model.getValue() != null && model.getValue() instanceof Boolean;
	}

	private Boolean calc() {
		if (isBoolean(d_bool1) && isBoolean(d_bool2)) {
			return (Boolean)d_bool1.getValue() && (Boolean)d_bool2.getValue();
		}
		return null;
	}
}

