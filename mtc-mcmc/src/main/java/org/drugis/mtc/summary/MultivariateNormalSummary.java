package org.drugis.mtc.summary;

public interface MultivariateNormalSummary extends Summary {

	public static final String PROPERTY_MEAN_VECTOR = "meanVector";
	public static final String PROPERTY_COVARIANCE_MATRIX = "covarianceMatrix";

	public abstract double[] getMeanVector();

	public abstract double[][] getCovarianceMatrix();

}