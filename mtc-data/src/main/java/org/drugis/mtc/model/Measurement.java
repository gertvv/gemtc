package org.drugis.mtc.model;

import java.beans.PropertyChangeListener;

import javax.xml.bind.annotation.XmlTransient;

import org.drugis.common.beans.ObserverManager;
import org.drugis.mtc.data.DataType;
import org.drugis.mtc.data.MeasurementData;

import com.jgoodies.binding.beans.Observable;

public class Measurement extends MeasurementData implements Observable {
	public static final String PROPERTY_TREATMENT = "treatment";
	public static final String PROPERTY_MEAN = "mean";
	public static final String PROPERTY_STDDEV = "stdDev";
	public static final String PROPERTY_SAMPLESIZE = "sampleSize";
	public static final String PROPERTY_RESPONDERS = "responders";
	
	@XmlTransient
	ObserverManager d_obsManager = new ObserverManager(this);
	
	public Measurement(Treatment t) {
		super();
		setTreatment(t);
	}
	
	public Measurement() {
		super();
	}
	
	@Override
	public Treatment getTreatment() {
		return (Treatment) super.getTreatment();
	}
	
	@Override
	public void setTreatment(Object value) {
		setTreatment((Treatment) value);
	}

	public void setTreatment(Treatment newValue) {
		Treatment oldValue = getTreatment();
		super.setTreatment(newValue);
		d_obsManager.firePropertyChange(PROPERTY_TREATMENT, oldValue, newValue);
	}
	
	@Override
	public void setMean(Double newValue) {
		Double oldValue = getMean();
		super.setMean(newValue);
		d_obsManager.firePropertyChange(PROPERTY_MEAN, oldValue, newValue);
	}
	
	@Override
	public void setStdDev(Double newValue) {
		Double oldValue = getStdDev();
		super.setStdDev(newValue);
		d_obsManager.firePropertyChange(PROPERTY_STDDEV, oldValue, newValue);
	}
	
	@Override
	public void setSampleSize(Integer newValue) {
		Integer oldValue = getSampleSize();
		super.setSampleSize(newValue);
		d_obsManager.firePropertyChange(PROPERTY_SAMPLESIZE, oldValue, newValue);
	}
	
	@Override
	public void setResponders(Integer newValue) {
		Integer oldValue = getResponders();
		super.setResponders(newValue);
		d_obsManager.firePropertyChange(PROPERTY_RESPONDERS, oldValue, newValue);
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
	 * Create a clone of this Measurement with only the properties allowed by the DataType set.
	 */
	public Measurement restrict(DataType type) {
		Measurement m = new Measurement();
		m.setTreatment(getTreatment());
		switch (type) {
		case NONE:
			break;
		case RATE:
			m.setSampleSize(getSampleSize());
			m.setResponders(getResponders());
			break;
		case CONTINUOUS:
			m.setMean(getMean());
			m.setStdDev(getStdDev());
			m.setSampleSize(getSampleSize());
			break;
		}
		return m;
	}
}
