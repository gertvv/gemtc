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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.binding.value.AbstractValueModel;

/**
 * Listens to a nested ValueModel, and converts it to true iff nested matches the given pattern.
 */
public class StringMatchesModel extends AbstractValueModel {
	private static final long serialVersionUID = -7062635813334832465L;

	private ValueModel d_nested;
	private final Pattern d_pattern;
	private boolean d_val;

	public StringMatchesModel(ValueModel nested, String regex) {
		d_nested = nested;
		d_pattern = Pattern.compile(regex);
		nested.addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				boolean oldVal = d_val;
				d_val = calc();
				fireValueChange(oldVal, d_val);
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

	private boolean calc() {
		if (d_nested.getValue() != null && d_nested.getValue() instanceof String) {
			Matcher matcher = d_pattern.matcher((String)d_nested.getValue());
			return matcher.matches();
		}
		return false;
	}
}
