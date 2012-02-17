package org.drugis.mtc.parameterization;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.drugis.mtc.model.JAXBHandler;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.junit.Test;

import edu.uci.ics.jung.algorithms.transformation.FoldingTransformerFixed.FoldedEdge;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.UndirectedGraph;


public class InconsistencyStartingValueGeneratorTest {
	private static final double EPSILON = 0.0000001;

	@Test
	public void testGenerate() throws JAXBException {
		Network network = JAXBHandler.readNetwork(InconsistencyParameterization.class.getResourceAsStream("network.xml"));
		
		Treatment ta = new Treatment("A");
		Treatment tb = new Treatment("B");
		Treatment tc = new Treatment("C");
		
		UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph = NetworkModel.createComparisonGraph(network);
		Tree<Treatment, FoldedEdge<Treatment, Study>> tree = new DelegateTree<Treatment, FoldedEdge<Treatment,Study>>();
		tree.addVertex(ta);
		tree.addEdge(cGraph.findEdge(ta, tb), ta, tb);
		tree.addEdge(cGraph.findEdge(ta, tc), ta, tc);
		Map<Partition, Set<List<Treatment>>> cycleClasses = InconsistencyParameterization.getCycleClasses(cGraph, tree);
		Map<Study, Treatment> baselines = InconsistencyParameterization.findStudyBaselines(network.getStudies(), cGraph, cycleClasses);
		InconsistencyParameterization pmtz = new InconsistencyParameterization(network, tree, cycleClasses, baselines);
		
		StartingValueGenerator generator = new ContinuousDataStartingValueGenerator(network, cGraph);
		
		Map<BasicParameter, Double> basicStart = new HashMap<BasicParameter, Double>();
		final BasicParameter pab = new BasicParameter(ta, tb);
		basicStart.put(pab, generator.getRelativeEffect(pab));
		final BasicParameter pac = new BasicParameter(ta, tc);
		basicStart.put(pac, generator.getRelativeEffect(pac));

		InconsistencyParameter incons = (InconsistencyParameter) pmtz.getParameters().get(2);
		assertEquals(0.48, InconsistencyStartingValueGenerator.generate(incons, pmtz, generator, basicStart), EPSILON);
	}
}
