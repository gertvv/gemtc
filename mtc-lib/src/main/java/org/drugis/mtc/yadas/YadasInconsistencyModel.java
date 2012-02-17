package org.drugis.mtc.yadas;

import java.util.ArrayList;
import java.util.List;

import org.drugis.mtc.InconsistencyModel;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.parameterization.InconsistencyParameterization;
import org.drugis.mtc.parameterization.Parameterization;

public class YadasInconsistencyModel extends YadasModel implements InconsistencyModel {

	public YadasInconsistencyModel(Network network) {
		super(network);
	}

	@Override
	protected Parameterization buildNetworkModel() {
		return InconsistencyParameterization.create(d_network);
	}

	@Override
	protected boolean isInconsistency() {
		return true;
	}

	@Override
	public List<Parameter> getInconsistencyFactors() {
		final List<? extends Parameter> parameters = d_pmtz.getParameters();
		return new ArrayList<Parameter>(parameters.subList(getNumberOfBasicParameters(), parameters.size()));
	}

	@Override
	public Parameter getInconsistencyVariance() {
		return d_inconsistencyVar;
	}

}
