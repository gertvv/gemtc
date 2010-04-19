package org.drugis.mtc;

/**
 * Factory that creates MTC models from evidence networks.
 */
public interface ModelFactory {
	/**
	 * Create a homogenous variance random effects consistency model.
	 */
	public <T extends Measurement> ConsistencyModel getConsistencyModel(Network<T> network);
	/**
	 * Create a homogenous variance random effects inconsistency model.
	 */
	public <T extends Measurement> InconsistencyModel getInconsistencyModel(Network<T> network);
}
