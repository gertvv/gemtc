package org.drugis.mtc.summary;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.drugis.common.beans.AbstractObservable;

public class ProxyMultivariateNormalSummary extends AbstractObservable implements MultivariateNormalSummary {

	private MultivariateNormalSummary d_nested;
	private PropertyChangeListener d_listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
		}
	};

	@Override
	public boolean getDefined() {
		return d_nested != null && d_nested.getDefined();
	}

	@Override
	public double[] getMeanVector() {
		return d_nested == null ? null : d_nested.getMeanVector();
	}

	@Override
	public double[][] getCovarianceMatrix() {
		return d_nested == null ? null : d_nested.getCovarianceMatrix();
	}

	public void setNested(MultivariateNormalSummary summary) {
		boolean wasDefined = getDefined();
		
		// remove old nested
		if (d_nested != null) {
			d_nested.removePropertyChangeListener(d_listener);
		}
		
		// set new nested
		d_nested = summary;
		if (d_nested != null) {
			d_nested.addPropertyChangeListener(d_listener);
		}
		
		// fire changes in defined
		if (wasDefined != getDefined()) {
			firePropertyChange(PROPERTY_DEFINED, wasDefined, getDefined());
		}
		// fire events if currently defined (with proper newValue), or if changing from defined to undefined (with null).
		if (getDefined() || wasDefined) {
			firePropertyChange(PROPERTY_MEAN_VECTOR, null, getDefined() ? getMeanVector() : null);
			firePropertyChange(PROPERTY_COVARIANCE_MATRIX, null, getDefined() ? getCovarianceMatrix() : null);
		}
	}
}
