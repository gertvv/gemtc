/*
 * This file is part of the GeMTC software for MTC model generation and
 * analysis. GeMTC is distributed from http://drugis.org/gemtc.
 * Copyright (C) 2009-2012 Gert van Valkenhoef.
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

package org.drugis.mtc.model;

import java.beans.PropertyChangeListener;

import javax.xml.bind.annotation.XmlTransient;

import org.drugis.common.beans.ObserverManager;
import org.drugis.mtc.data.TreatmentData;

import com.jgoodies.binding.beans.Observable;

public class Treatment extends TreatmentData implements Observable, Comparable<Treatment> {
	public static final String PROPERTY_DESCRIPTION = "description";
	public static final String PROPERTY_ID = "id";

	@XmlTransient
	ObserverManager d_obsManager = new ObserverManager(this);

	public Treatment() {
		super();
	}

	public Treatment(String id, String desc) {
		super();
		setId(id);
		setDescription(desc);
	}

	public Treatment(String id) {
		this(id, "");
	}

	@Override
	public void setId(String newValue) {
		String oldValue = getId();
		super.setId(newValue);
		d_obsManager.firePropertyChange(PROPERTY_ID, oldValue, newValue);
	}

	public void setDescription(String newValue) {
		setValue(newValue);
	}

	public String getDescription() {
		return getValue();
	}

	/**
	 * An alias for setDescription(). For JAXB compatibility. Do not use.
	 */
	@Override
	public void setValue(String newValue) {
		String oldValue = super.getValue();
		super.setValue(newValue);
		d_obsManager.firePropertyChange(PROPERTY_DESCRIPTION, oldValue, newValue);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		d_obsManager.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		d_obsManager.removePropertyChangeListener(listener);
	}

	@Override
	public String toString() {
		return "Treatment[" + getId() + "]";
	}

	@Override
	public int compareTo(Treatment t) {
		return getId().compareTo(t.getId());
	}

	public String format() {
		if(getDescription() == null || getDescription().isEmpty()) {
			return getId();
		}
		return getDescription();
	}

	public String format(boolean preferDescription) {
		return preferDescription ? format() : getId();
	}
}
