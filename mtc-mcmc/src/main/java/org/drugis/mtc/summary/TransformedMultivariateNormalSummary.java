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

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.drugis.common.beans.AbstractObservable;

public class TransformedMultivariateNormalSummary extends AbstractObservable implements MultivariateNormalSummary {

	private final MultivariateNormalSummary d_nested;
	private final RealMatrix d_transform;
	private boolean d_isDefined;
	private RealMatrix d_means;
	private RealMatrix d_covarianceMatrix;

	public TransformedMultivariateNormalSummary(MultivariateNormalSummary nested, double[][] matrix) {
		d_nested = nested;
		d_transform = new Array2DRowRealMatrix(matrix, true);
		d_nested.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				transform();
			}
		});
		transform();
	}

	public boolean getDefined() {
		return d_isDefined;
	}
	
	public double[] getMeanVector() {
		return d_means.getColumn(0);
	}
	
	public double[][] getCovarianceMatrix() {
		return d_covarianceMatrix.getData();
	}

	protected void transform() {
		if (!d_nested.getDefined()) {
			d_isDefined = false;
			return;
		}
		
		d_means = d_transform.multiply(new Array2DRowRealMatrix(d_nested.getMeanVector()));
		d_covarianceMatrix = d_transform.multiply(new Array2DRowRealMatrix(d_nested.getCovarianceMatrix(), true).multiply(d_transform.transpose()));
		
		d_isDefined = true;
		
		firePropertyChange(PROPERTY_DEFINED, null, d_isDefined);
		firePropertyChange(MultivariateNormalSummary.PROPERTY_MEAN_VECTOR, null, getMeanVector());
		firePropertyChange(MultivariateNormalSummary.PROPERTY_COVARIANCE_MATRIX, null, getCovarianceMatrix());
	}	
}
