package org.drugis.mtc.model;

import java.beans.PropertyChangeListener;

import javax.xml.bind.annotation.XmlTransient;

import org.drugis.common.beans.ObserverManager;
import org.drugis.mtc.data.StudyData;

import com.jgoodies.binding.beans.Observable;

public class Study extends StudyData implements Observable {
	public static final String PROPERTY_ID = "id";
	
	@XmlTransient
	ObserverManager d_obsManager = new ObserverManager(this);
	
	@Override
	public void setId(String newValue) {
		String oldValue = getId();
		super.setId(newValue);
		d_obsManager.firePropertyChange(PROPERTY_ID, oldValue, newValue);
	}
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		d_obsManager.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		d_obsManager.addPropertyChangeListener(listener);
	}
}