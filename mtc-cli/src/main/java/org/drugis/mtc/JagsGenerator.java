package org.drugis.mtc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.math.random.JDKRandomGenerator;
import org.drugis.mtc.graph.MinimumDiameterSpanningTree;
import org.drugis.mtc.jags.JagsSyntaxModel;
import org.drugis.mtc.model.JAXBHandler;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.AbstractDataStartingValueGenerator;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.parameterization.ConsistencyParameterization;
import org.drugis.mtc.parameterization.InconsistencyParameterization;
import org.drugis.mtc.parameterization.NetworkModel;
import org.drugis.mtc.parameterization.NodeSplitParameterization;
import org.drugis.mtc.parameterization.Parameterization;

import edu.uci.ics.jung.algorithms.transformation.FoldingTransformerFixed.FoldedEdge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.util.Pair;

public class JagsGenerator {
	private final Options d_options;

	public JagsGenerator(Options options) {
		d_options = options;
	}
	
	public void run() throws FileNotFoundException, JAXBException {
		Network network = JAXBHandler.readNetwork(new FileInputStream(d_options.getXmlFile()));

		switch (network.getType()) {
		case RATE:
		case CONTINUOUS:
			generateModel(network);
			break;
		default:
			System.out.println("Unsupported measurement type, only generating spanning tree");
			generateTree(network);
			break;
		}
	}

	private void generateModel(Network network) throws FileNotFoundException {
		for (ModelSpecification spec : createJagsModels(network)) {
			writeModel(spec);
		}
	}

	private List<ModelSpecification> createJagsModels(Network network) {
		switch (d_options.getModelType()) {
		case CONSISTENCY:
			ConsistencyParameterization cons = ConsistencyParameterization.create(network);
			return Collections.singletonList(createJagsModel(network, cons, ".cons"));
		case INCONSISTENCY:
			InconsistencyParameterization inco = InconsistencyParameterization.create(network);
			return Collections.singletonList(createJagsModel(network, inco, ".inco"));
		case NODESPLIT:
			List<ModelSpecification> list = new ArrayList<ModelSpecification>();
			for (BasicParameter p : NodeSplitParameterization.getSplittableNodes(NetworkModel.createStudyGraph(network), NetworkModel.createComparisonGraph(network))) {
				NodeSplitParameterization splt = NodeSplitParameterization.create(network, p);
				list.add(createJagsModel(network, splt, ".splt." + p.getBaseline().getId() + "." + p.getSubject().getId()));
			}
			return list;
		default:
			throw new IllegalArgumentException("Unknown model type " + d_options.getModelType());
		}
	}

	private ModelSpecification createJagsModel(Network network, Parameterization pmtz, String suffix) {
		return new ModelSpecification(new JagsSyntaxModel(network, pmtz, !d_options.getBugsOutput()),
				AbstractDataStartingValueGenerator.create(network, NetworkModel.createComparisonGraph(network), 
						new JDKRandomGenerator(), d_options.getScale()),
				suffix);
	}

	private void generateTree(Network network) {
		MinimumDiameterSpanningTree<Treatment, FoldedEdge<Treatment, Study>> finder =
			new MinimumDiameterSpanningTree<Treatment, FoldedEdge<Treatment, Study>>(NetworkModel.createComparisonGraph(network));
		Tree<Treatment, FoldedEdge<Treatment, Study>> tree = finder.getMinimumDiameterSpanningTree();
		printGraph(tree);
	}
	
	private void printGraph(Graph<Treatment, FoldedEdge<Treatment, Study>> g) {
		System.out.println("\tgraph {");
		for (FoldedEdge<Treatment, Study> e : g.getEdges()) {
			Pair<Treatment> t = new Pair<Treatment>(g.getIncidentVertices(e));
			System.out.println("\t\t" + t.getFirst().getId() + " -- " + t.getSecond().getId());
		}
		System.out.println("\t}");
	}
	
	public void writeModel(ModelSpecification spec) throws FileNotFoundException {
		// FIXME: enable printing again
//		printGraph(spec.getModel().model.network.treatmentGraph)
//		printTree(spec.getModel().model.basis.tree)

		if (d_options.getSuppressOutput()) {
			return;
		}

		System.out.println("Writing JAGS scripts: " + d_options.getBaseName() + spec.getNameSuffix() + ".*");

		PrintStream dataOut = new PrintStream(d_options.getBaseName() + spec.getNameSuffix() + ".data");
		dataOut.println(spec.getModel().dataText());
		dataOut.close();

		PrintStream modelOut = new PrintStream(d_options.getBaseName() + spec.getNameSuffix() + ".model");
		modelOut.println(spec.getModel().modelText());
		modelOut.close();

		int nChains = 4;

		PrintStream scriptOut = new PrintStream(d_options.getBaseName() + spec.getNameSuffix() + ".script");
		scriptOut.println(spec.getModel().scriptText(d_options.getBaseName() + spec.getNameSuffix(), nChains,
				d_options.getTuningIterations(), d_options.getSimulationIterations()));
		scriptOut.close();

		for (int i = 0; i < nChains; ++i) {
			PrintStream paramOut = new PrintStream(d_options.getBaseName() + spec.getNameSuffix() + ".inits" + i);
			paramOut.println(spec.getModel().initialValuesText(spec.getGenerator()));
			paramOut.close();
		}

		if (d_options.getModelType() != ModelType.NODESPLIT) { // FIXME: implement for nodesplit
			PrintStream analysisOut = new PrintStream(d_options.getBaseName() + spec.getNameSuffix() + ".analysis.R");
			analysisOut.println(spec.getModel().analysisText(d_options.getBaseName() + spec.getNameSuffix()));
			analysisOut.close();
		}
	}
}