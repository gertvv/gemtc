package org.drugis.mtc.yadas;

import java.util.List;

import org.drugis.mtc.ConsistencyModel;
import org.drugis.mtc.InconsistencyModel;
import org.drugis.mtc.ModelFactory;
import org.drugis.mtc.NodeSplitModel;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.parameterization.NetworkModel;
import org.drugis.mtc.parameterization.NodeSplitParameterization;

import edu.uci.ics.jung.graph.Hypergraph;

public class YadasModelFactory implements ModelFactory {
	@Override
	public ConsistencyModel getConsistencyModel(Network network) {
		return new YadasConsistencyModel(network);
	}

	@Override
	public InconsistencyModel getInconsistencyModel(Network network) {
		return new YadasInconsistencyModel(network);
	}

	@Override
	public NodeSplitModel getNodeSplitModel(Network network, BasicParameter split) {
		return new YadasNodeSplitModel(network, split);
	}

	@Override
	public List<BasicParameter> getSplittableNodes(Network network) {
		final Hypergraph<Treatment, Study> studyGraph = NetworkModel.createStudyGraph(network);
		return NodeSplitParameterization.getSplittableNodes(studyGraph, NetworkModel.createComparisonGraph(studyGraph));
	}
}
