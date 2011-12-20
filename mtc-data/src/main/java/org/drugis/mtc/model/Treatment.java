package org.drugis.mtc.model;

import java.beans.PropertyChangeListener;

import javax.xml.bind.annotation.XmlTransient;

import org.drugis.common.beans.ObserverManager;
import org.drugis.mtc.data.TreatmentData;

import com.jgoodies.binding.beans.Observable;

public class Treatment extends TreatmentData implements Observable {
	private static final String PROPERTY_DESCRIPTION = "description";
	private static final String PROPERTY_ID = "id";
	@XmlTransient
	ObserverManager d_obsManager = new ObserverManager(this);
	
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
	
	@Override
	public void setValue(String newValue) {
		String oldValue = super.getValue();
		super.setValue(value);
		d_obsManager.firePropertyChange(PROPERTY_DESCRIPTION, oldValue, newValue);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		d_obsManager.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		d_obsManager.addPropertyChangeListener(listener);
	}
	
	@Override
	public String toString() {
		return "Treatment[" + getId() + "]{description=" + getDescription() + "}";
	}
}
