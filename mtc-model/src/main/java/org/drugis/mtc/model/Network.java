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
import org.drugis.common.beans.SortedSetModel;
import org.drugis.mtc.data.DataType;
import org.drugis.mtc.data.NetworkData;
import org.drugis.mtc.data.StudyData;
import org.drugis.mtc.data.TreatmentData;

import com.jgoodies.binding.beans.Observable;
import com.jgoodies.binding.list.ObservableList;

public class Network extends NetworkData implements Observable {
	public static final String PROPERTY_DESCRIPTION = "description";
	public static final String PROPERTY_TYPE = "type";
	
	private static class SortedStudies extends NetworkData.Studies {
		public SortedStudies(NetworkData.Studies studies) {
			this.study = new SortedSetModel<StudyData>(studies.getStudy());
		}
		public SortedStudies() {
			this.study = new SortedSetModel<StudyData>();
		}
	}
	
	private static class SortedTreatments extends NetworkData.Treatments {
		public SortedTreatments(NetworkData.Treatments treatments) {
			this.treatment = new SortedSetModel<TreatmentData>(treatments.getTreatment());
		}
		public SortedTreatments() {
			this.treatment = new SortedSetModel<TreatmentData>();
		}
	}
	
	@XmlTransient
	ObserverManager d_obsManager = new ObserverManager(this);
	
	public Network() {
		this.studyList = new SortedStudies();
		this.treatmentList = new SortedTreatments();
	}
	
	public Network(NetworkData data) {
		this.description = data.getDescription();
		this.type = data.getType();
		this.studyList = new SortedStudies(data.getStudyList());
		this.treatmentList = new SortedTreatments(data.getTreatmentList());
	}
	
	@Override
	public void setType(DataType newValue) {
		DataType oldValue = super.getType();
		super.setType(newValue);
		d_obsManager.firePropertyChange(PROPERTY_TYPE, oldValue, newValue);
	}
	
	@Override
	public void setDescription(String newValue) {
		String oldValue = getDescription();
		super.setDescription(newValue);
		d_obsManager.firePropertyChange(PROPERTY_DESCRIPTION, oldValue, newValue);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ObservableList<Treatment> getTreatments() {
		return (ObservableList<Treatment>) (ObservableList) super.getTreatmentList().getTreatment();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ObservableList<Study> getStudies() {
		return (ObservableList<Study>) (ObservableList) super.getStudyList().getStudy();
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		d_obsManager.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		d_obsManager.removePropertyChangeListener(listener);
	}

	/**
	 * Create a clone of this Network, with the Measurement values restricted to those allowed by the DataType.
	 */
	public Network restrictMeasurements() {
		Network n = new Network();
		n.setDescription(getDescription());
		n.setType(getType());
		n.setTreatmentList(getTreatmentList());
		n.setStudyList(new Studies());
		for (Study s : getStudies()) {
			n.getStudies().add(s.restrictMeasurements(type));
		}
		return n;
	}
}
