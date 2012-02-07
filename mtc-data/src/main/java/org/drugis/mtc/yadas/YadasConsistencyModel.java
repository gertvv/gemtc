package org.drugis.mtc.yadas;

import org.drugis.mtc.ConsistencyModel;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.parameterization.ConsistencyParameterization;
import org.drugis.mtc.parameterization.Parameterization;

public class YadasConsistencyModel extends YadasModel implements ConsistencyModel {

	public YadasConsistencyModel(Network network) {
		super(network);
	}

	@Override
	protected Parameterization buildNetworkModel() {
		return ConsistencyParameterization.create(d_network);
	}

	@Override
	protected boolean isInconsistency() {
		return false;
	}

}
