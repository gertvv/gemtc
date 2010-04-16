package org.drugis.mtc;

/**
 * Factory that creates MTC models from evidence networks.
 */
public interface ModelFactory {
	/**
	 * Create a homogenous variance random effects consistency model.
	 */
	public ConsistencyModel getConsistencyModel(Network<DichotomousMeasurement> network);
	/**
	 * Create a homogenous variance random effects inconsistency model.
	 */
	public InconsistencyModel getInconsistencyModel(Network<DichotomousMeasurement> network);
}
