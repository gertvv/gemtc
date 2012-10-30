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

package org.drugis.mtc.summary;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.drugis.common.beans.AbstractObservable;

public class ProxyMultivariateNormalSummary extends AbstractObservable implements MultivariateNormalSummary {

	private MultivariateNormalSummary d_nested;
	private PropertyChangeListener d_listener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
		}
	};

	public boolean getDefined() {
		return d_nested != null && d_nested.getDefined();
	}

	public double[] getMeanVector() {
		return d_nested == null ? null : d_nested.getMeanVector();
	}

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
