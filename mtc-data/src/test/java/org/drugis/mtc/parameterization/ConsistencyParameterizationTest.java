package org.drugis.mtc.parameterization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.junit.Before;
import org.junit.Test;

import edu.uci.ics.jung.algorithms.transformation.FoldingTransformerFixed.FoldedEdge;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.UndirectedGraph;

public class ConsistencyParameterizationTest {
	private Network d_network;
	private Study d_s1;
	private Study d_s2;
	private Study d_s3;
	private Study d_s4;
	private Treatment d_ta;
	private Treatment d_tb;
	private Treatment d_tc;
	private Treatment d_td;
	private Treatment d_te;
	private Treatment d_tf;
	
	@Before
	public void setUp() {
		d_network = new Network();
		d_ta = new Treatment("A");
		d_tb = new Treatment("B");
		d_tc = new Treatment("C");
		d_td = new Treatment("D");
		d_te = new Treatment("E");
		d_tf = new Treatment("F");
		d_network.getTreatments().addAll(Arrays.asList(d_ta, d_tb, d_tc, d_td));
		d_s1 = new Study("1");
		d_s1.getMeasurements().addAll(Arrays.asList(new Measurement(d_td), new Measurement(d_tb), new Measurement(d_tc)));
		d_s2 = new Study("2");
		d_s2.getMeasurements().addAll(Arrays.asList(new Measurement(d_ta), new Measurement(d_tb)));
		d_s3 = new Study("3");
		d_s3.getMeasurements().addAll(Arrays.asList(new Measurement(d_ta), new Measurement(d_tc)));
		d_s4 = new Study("4");
		d_s4.getMeasurements().addAll(Arrays.asList(new Measurement(d_ta), new Measurement(d_td)));
		d_network.getStudies().addAll(Arrays.asList(d_s1, d_s2, d_s3, d_s4));
	}
	
	@Test
	public void testFindSpanningTree() {
		UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph = NetworkModel.createComparisonGraph(d_network);
		Tree<Treatment, FoldedEdge<Treatment, Study>> tree = ConsistencyParameterization.findSpanningTree(cGraph);
		assertEquals(d_ta, tree.getRoot());
		assertNotNull(tree.findEdge(d_ta, d_tb));
		assertNotNull(tree.findEdge(d_ta, d_tc));
		assertNotNull(tree.findEdge(d_ta, d_td));
	}
	
	@Test
	public void testFindStudyBaselines() {
		Network network = new Network();
		d_network.getTreatments().addAll(Arrays.asList(d_ta, d_tb, d_tc, d_td, d_te, d_tf));
		Study s1 = new Study("1");
		s1.getMeasurements().addAll(Arrays.asList(new Measurement(d_tc), new Measurement(d_tf)));
		Study s2 = new Study("2");
		s2.getMeasurements().addAll(Arrays.asList(new Measurement(d_tb), new Measurement(d_tc), new Measurement(d_td)));
		Study s3 = new Study("3");
		s3.getMeasurements().addAll(Arrays.asList(new Measurement(d_ta), new Measurement(d_te), new Measurement(d_tf)));
		network.getStudies().addAll(Arrays.asList(s1, s2, s3));
		
		// First test the tree is as expected since the study baselines depend on the chosen tree
		Hypergraph<Treatment, Study> studyGraph = NetworkModel.createStudyGraph(network);
		UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph = NetworkModel.createComparisonGraph(studyGraph);
		Tree<Treatment, FoldedEdge<Treatment, Study>> tree = ConsistencyParameterization.findSpanningTree(cGraph);
		assertEquals(d_tc, tree.getRoot());
		assertNotNull(tree.findEdge(d_tc, d_tf));
		assertNotNull(tree.findEdge(d_tc, d_tb));
		assertNotNull(tree.findEdge(d_tc, d_td));
		assertNotNull(tree.findEdge(d_tf, d_te));
		assertNotNull(tree.findEdge(d_tf, d_ta));
		
		// Now test the study baselines
		Map<Study, Treatment> baselines = ConsistencyParameterization.findStudyBaselines(studyGraph, tree);
		assertEquals(d_tc, baselines.get(s1));
		assertEquals(d_tc, baselines.get(s2));
		assertEquals(d_tf, baselines.get(s3));
	}

	@Test
	public void testBasicParameters() {
		ConsistencyParameterization pmtz = ConsistencyParameterization.create(d_network);
		
		List<NetworkParameter> expected = new ArrayList<NetworkParameter>();
		expected.add(new BasicParameter(d_ta, d_tb));
		expected.add(new BasicParameter(d_ta, d_tc));
		expected.add(new BasicParameter(d_ta, d_td));
		assertEquals(expected, pmtz.getParameters());
	}
	
	@Test
	public void testStudyBaselines() {
		ConsistencyParameterization pmtz = ConsistencyParameterization.create(d_network);

		assertEquals(d_tb, pmtz.getStudyBaseline(d_s1));
		assertEquals(d_ta, pmtz.getStudyBaseline(d_s2));
		assertEquals(d_ta, pmtz.getStudyBaseline(d_s3));
		assertEquals(d_ta, pmtz.getStudyBaseline(d_s4));
	}
	
	@Test
	public void testParameterizationBasic() {
		ConsistencyParameterization pmtz = ConsistencyParameterization.create(d_network);
		
		Map<NetworkParameter, Integer> expected1 = new HashMap<NetworkParameter, Integer>();
		expected1.put(new BasicParameter(d_ta, d_tb), 1);
		assertEquals(expected1, pmtz.parameterize(d_ta, d_tb));
		
		Map<NetworkParameter, Integer> expected2 = new HashMap<NetworkParameter, Integer>();
		expected2.put(new BasicParameter(d_ta, d_tb), -1);
		assertEquals(expected2, pmtz.parameterize(d_tb, d_ta));		
	}
	
	@Test
	public void testParameterizationFunctional() {
		ConsistencyParameterization pmtz = ConsistencyParameterization.create(d_network);

		Map<NetworkParameter, Integer> expected1 = new HashMap<NetworkParameter, Integer>();
		expected1.put(new BasicParameter(d_ta, d_tb), -1);
		expected1.put(new BasicParameter(d_ta, d_tc), 1);
		assertEquals(expected1, pmtz.parameterize(d_tb, d_tc));
		
		Map<NetworkParameter, Integer> expected2 = new HashMap<NetworkParameter, Integer>();
		expected2.put(new BasicParameter(d_ta, d_tb), 1);
		expected2.put(new BasicParameter(d_ta, d_tc), -1);
		assertEquals(expected2, pmtz.parameterize(d_tc, d_tb));
	}
}
