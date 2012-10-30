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
	
	public Measurement(Treatment t, int responders, int sampleSize) {
		this(t);
		setResponders(responders);
		setSampleSize(sampleSize);
	}
	
	public Measurement(Treatment t, double mean, double stdDev, int sampleSize) {
		this(t);
		setMean(mean);
		setStdDev(stdDev);
		setSampleSize(sampleSize);
	}
	
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
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		d_obsManager.addPropertyChangeListener(listener);
	}

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
