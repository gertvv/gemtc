package org.drugis.mtc.model;

import java.beans.PropertyChangeListener;

import javax.xml.bind.annotation.XmlTransient;

import org.drugis.common.beans.ObserverManager;
import org.drugis.mtc.data.MeasurementData;

import com.jgoodies.binding.beans.Observable;

public class Measurement extends MeasurementData implements Observable {
	@XmlTransient
	ObserverManager d_obsManager = new ObserverManager(this);
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		d_obsManager.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		d_obsManager.removePropertyChangeListener(listener);
	}
}
