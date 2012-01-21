package org.drugis.mtc.model;

import java.beans.PropertyChangeListener;

import javax.xml.bind.annotation.XmlTransient;

import org.drugis.common.beans.ObserverManager;
import org.drugis.mtc.data.DataType;
import org.drugis.mtc.data.NetworkData;

import com.jgoodies.binding.beans.Observable;
import com.jgoodies.binding.list.ObservableList;

public class Network extends NetworkData implements Observable {
	public static final String PROPERTY_DESCRIPTION = "description";
	public static final String PROPERTY_TYPE = "type";
	
	@XmlTransient
	ObserverManager d_obsManager = new ObserverManager(this);
	
	public Network() {
	}
	
	public Network(NetworkData data) {
		this.description = data.getDescription();
		this.type = data.getType();
		this.studyList = data.getStudyList();
		this.treatmentList = data.getTreatmentList();
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
	
	@SuppressWarnings("unchecked")
	public ObservableList<Treatment> getTreatments() {
		return (ObservableList<Treatment>) (ObservableList) super.getTreatmentList().getTreatment();
	}
	
	@SuppressWarnings("unchecked")
	public ObservableList<Study> getStudies() {
		return (ObservableList<Study>) (ObservableList) super.getStudyList().getStudy();
	}
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		d_obsManager.addPropertyChangeListener(listener);
	}

	@Override
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
