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
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.drugis.common.EqualsUtil;
import org.drugis.common.beans.ObserverManager;
import org.drugis.mtc.data.DataType;
import org.drugis.mtc.data.StudyData;

import com.jgoodies.binding.beans.Observable;
import com.jgoodies.binding.list.ObservableList;

public class Study extends StudyData implements Observable, Comparable<Study> {
	public static final String PROPERTY_ID = "id";
	
	@XmlTransient
	ObserverManager d_obsManager = new ObserverManager(this);
	
	public Study() {
		super();
	}
	
	public Study(String id) {
		super();
		setId(id);
	}

	@Override
	public void setId(String newValue) {
		String oldValue = getId();
		super.setId(newValue);
		d_obsManager.firePropertyChange(PROPERTY_ID, oldValue, newValue);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ObservableList<Measurement> getMeasurements() {
		return (ObservableList) super.getMeasurementList();
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		d_obsManager.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		d_obsManager.addPropertyChangeListener(listener);
	}
	
	@Override
	public String toString() {
		return "Study[" + getId() + "]";
	}
	
	/**
	 * Create a clone of this Study, with the Measurement values restricted to those allowed by the DataType.
	 */
	public Study restrictMeasurements(DataType type) {
		Study s = new Study();
		s.setId(getId());
		for (Measurement m : getMeasurements()) {
			s.getMeasurements().add(m.restrict(type));
		}
		return s;
	}

	public boolean containsTreatment(final Treatment t) {
		return CollectionUtils.exists(getMeasurements(), new Predicate<Measurement>() {
			public boolean evaluate(Measurement m) {
				return m.getTreatment().equals(t);
			}
		});
	}
	
	public Set<Treatment> getTreatments() {
		Set<Treatment> treatments = new HashSet<Treatment>();
		for (Measurement m : getMeasurements()) {
			treatments.add(m.getTreatment());
		}
		return treatments;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof Study) {
			Study other = (Study) o;
			return EqualsUtil.equal(getId(), other.getId());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return EqualsUtil.hashCode(getId());
	}

	public int compareTo(Study s) {
		return getId().compareTo(s.getId());
	}
}
