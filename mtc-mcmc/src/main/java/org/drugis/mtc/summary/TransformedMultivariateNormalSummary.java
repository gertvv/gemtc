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

	@Override
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
