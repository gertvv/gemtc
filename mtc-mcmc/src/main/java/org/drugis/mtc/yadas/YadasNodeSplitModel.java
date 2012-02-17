package org.drugis.mtc.yadas;

import java.util.Map;

import org.drugis.mtc.NodeSplitModel;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.parameterization.NetworkParameter;
import org.drugis.mtc.parameterization.NodeSplitParameterization;
import org.drugis.mtc.parameterization.Parameterization;
import org.drugis.mtc.parameterization.SplitParameter;

public class YadasNodeSplitModel extends YadasModel implements NodeSplitModel {
	private final BasicParameter d_split;

	public YadasNodeSplitModel(Network network, BasicParameter split) {
		super(network);
		d_split = split;
	}

	@Override
	protected Parameterization buildNetworkModel() {
		return NodeSplitParameterization.create(d_network, d_split);
	}
	
	@Override
	protected Map<NetworkParameter, Derivation> getDerivedParameters() {
		final Map<NetworkParameter, Derivation> map = super.getDerivedParameters();
		map.put(getIndirectEffect(), new Derivation(((NodeSplitParameterization)d_pmtz).parameterizeIndirect()));
		return map;
	}

	@Override
	protected boolean isInconsistency() {
		return false;
	}

	@Override
	public NetworkParameter getDirectEffect() {
		return new SplitParameter(d_split.getBaseline(), d_split.getSubject(), true);
	}

	@Override
	public NetworkParameter getIndirectEffect() {
		return new SplitParameter(d_split.getBaseline(), d_split.getSubject(), false);
	}

	@Override
	public BasicParameter getSplitNode() {
		return d_split;
	}
}
