package org.drugis.mtc.model;

import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.drugis.common.beans.ObserverManager;
import org.drugis.mtc.data.DataType;
import org.drugis.mtc.data.StudyData;

import com.jgoodies.binding.beans.Observable;
import com.jgoodies.binding.list.ObservableList;

public class Study extends StudyData implements Observable {
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
	
	@SuppressWarnings("unchecked")
	public ObservableList<Measurement> getMeasurements() {
		return (ObservableList) super.getMeasurementList();
	}
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		d_obsManager.addPropertyChangeListener(listener);
	}

	@Override
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
}