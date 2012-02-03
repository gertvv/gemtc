package org.drugis.mtc.parameterization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.junit.Before;
import org.junit.Test;

import edu.uci.ics.jung.algorithms.transformation.FoldingTransformerFixed.FoldedEdge;
import edu.uci.ics.jung.graph.Tree;

public class NodeSplitParameterizationTest {
	private Network d_network;
	private Study d_s1;
	private Study d_s2;
	private Study d_s3;
	private Study d_s4;
	private Study d_s5;
	private Treatment d_ta;
	private Treatment d_tb;
	private Treatment d_tc;
	private Treatment d_td;
	private Treatment d_te;
	
	@Before
	public void setUp() {
		d_ta = new Treatment("A");
		d_tb = new Treatment("B");
		d_tc = new Treatment("C");
		d_td = new Treatment("D");
		d_te = new Treatment("E");
		
		d_s1 = new Study();
		d_s1.getMeasurements().addAll(Arrays.asList(new Measurement(d_ta), new Measurement(d_tb)));
		d_s2 = new Study();
		d_s2.getMeasurements().addAll(Arrays.asList(new Measurement(d_ta), new Measurement(d_tc)));
		d_s3 = new Study();
		d_s3.getMeasurements().addAll(Arrays.asList(new Measurement(d_tb), new Measurement(d_tc)));
		d_s4 = new Study();
		d_s4.getMeasurements().addAll(Arrays.asList(new Measurement(d_tc), new Measurement(d_td)));
		d_s5 = new Study();
		d_s5.getMeasurements().addAll(Arrays.asList(new Measurement(d_tc), new Measurement(d_te)));
		d_network = new Network();
		d_network.getTreatments().addAll(Arrays.asList(d_ta, d_tb, d_tc, d_td, d_te));
		d_network.getStudies().addAll(Arrays.asList(d_s1, d_s2, d_s3, d_s4, d_s5));
	}
	
	@Test
	public void testSplittableNodes() {
		assertEquals(Arrays.asList(new BasicParameter(d_ta, d_tb), new BasicParameter(d_ta, d_tc), new BasicParameter(d_tb, d_tc)),
				NodeSplitParameterization.getSplittableNodes(NetworkModel.createStudyGraph(d_network), NetworkModel.createComparisonGraph(d_network)));
		
		d_s3.getMeasurements().add(new Measurement(d_ta));
		assertEquals(Arrays.asList(new BasicParameter(d_tb, d_tc)),
				NodeSplitParameterization.getSplittableNodes(NetworkModel.createStudyGraph(d_network), NetworkModel.createComparisonGraph(d_network)));
	}
	
	@Test
	public void testSplittableNodesNone() {
		Study s1 = new Study();
		s1.getMeasurements().addAll(Arrays.asList(new Measurement(d_ta), new Measurement(d_tb)));
		Study s2 = new Study();
		s2.getMeasurements().addAll(Arrays.asList(new Measurement(d_ta), new Measurement(d_tc)));
		Network network = new Network();
		network.getTreatments().addAll(Arrays.asList(d_ta, d_tb, d_tc));
		network.getStudies().addAll(Arrays.asList(s1, s2));
		
		assertEquals(Collections.emptyList(),
				NodeSplitParameterization.getSplittableNodes(NetworkModel.createStudyGraph(network), NetworkModel.createComparisonGraph(network)));
	}
	
	@Test
	public void testSplittableNodesFourArm() {
		Study s1 = new Study();
		s1.getMeasurements().addAll(Arrays.asList(new Measurement(d_ta), new Measurement(d_tb)));
		Study s2 = new Study();
		s2.getMeasurements().addAll(Arrays.asList(new Measurement(d_tc), new Measurement(d_td)));
		Study s3 = new Study();
		s3.getMeasurements().addAll(Arrays.asList(new Measurement(d_ta), new Measurement(d_tb), new Measurement(d_tc), new Measurement(d_td)));
		Network network = new Network();
		network.getTreatments().addAll(Arrays.asList(d_ta, d_tb, d_tc, d_td));
		network.getStudies().addAll(Arrays.asList(s1, s2, s3));
		
		assertEquals(Arrays.asList(new BasicParameter(d_ta, d_tc), new BasicParameter(d_ta, d_td), new BasicParameter(d_tb, d_tc), new BasicParameter(d_tb, d_td)),
				NodeSplitParameterization.getSplittableNodes(NetworkModel.createStudyGraph(network), NetworkModel.createComparisonGraph(network)));

	}

	@Test
	public void testFindSpanningTree() {
		Tree<Treatment, FoldedEdge<Treatment, Study>> tree = NodeSplitParameterization.findSpanningTree(NetworkModel.createComparisonGraph(d_network), new BasicParameter(d_ta, d_tb));
		assertEquals(4, tree.getEdgeCount());
		assertNotNull(tree.findEdge(d_tc, d_ta));
		assertNotNull(tree.findEdge(d_tc, d_tb));
		assertNotNull(tree.findEdge(d_tc, d_td));
		assertNotNull(tree.findEdge(d_tc, d_te));
	}
	
	@Test
	public void testAssignBaselines() {
		Study s1 = new Study();
		s1.getMeasurements().addAll(Arrays.asList(new Measurement(d_ta), new Measurement(d_tb)));
		Study s2 = new Study();
		s2.getMeasurements().addAll(Arrays.asList(new Measurement(d_tc), new Measurement(d_td)));
		Study s3 = new Study();
		s3.getMeasurements().addAll(Arrays.asList(new Measurement(d_ta), new Measurement(d_tb), new Measurement(d_tc), new Measurement(d_td)));
		Network network = new Network();
		network.getTreatments().addAll(Arrays.asList(d_ta, d_tb, d_tc, d_td));
		network.getStudies().addAll(Arrays.asList(s1, s2, s3));
		
		BasicParameter split = new BasicParameter(d_ta, d_tc);
		Tree<Treatment,FoldedEdge<Treatment,Study>> tree = NodeSplitParameterization.findSpanningTree(NetworkModel.createComparisonGraph(network), split);
		Map<Study, Treatment> baselines = NodeSplitParameterization.findStudyBaselines(NetworkModel.createStudyGraph(network), tree, split);
		
		assertTrue(baselines.keySet().containsAll(network.getStudies()));
		assertEquals(tree.getRoot(), baselines.get(s3));
	}
	
	@Test
	public void testBasicParameters() {
		NodeSplitParameterization pmtz = NodeSplitParameterization.create(d_network, new BasicParameter(d_ta, d_tb));
		
		List<NetworkParameter> expected = Arrays.<NetworkParameter>asList(
				new BasicParameter(d_tc, d_ta), new BasicParameter(d_tc, d_tb), 
				new BasicParameter(d_tc, d_td), new BasicParameter(d_tc, d_te),
				new SplitParameter(d_ta, d_tb, true));
		
		assertEquals(expected, pmtz.getParameters());
	}

}
