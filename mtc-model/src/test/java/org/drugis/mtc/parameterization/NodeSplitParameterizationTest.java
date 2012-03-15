/*
 * This file is part of the GeMTC software for MTC model generation and
 * analysis. GeMTC is distributed from http://drugis.org/gemtc.
 * Copyright (C) 2009-2012 Gert van Valkenhoef.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.drugis.mtc.parameterization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
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
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.util.Pair;

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

	@Test
	public void testParameterize() {
		NodeSplitParameterization pmtz = NodeSplitParameterization.create(d_network, new BasicParameter(d_ta, d_tb));

		// The split comparison should be parameterized using the direct node.
		assertEquals(Collections.singletonMap(new SplitParameter(d_ta, d_tb, true), 1), pmtz.parameterize(d_ta, d_tb));
		assertEquals(Collections.singletonMap(new SplitParameter(d_ta, d_tb, true), -1), pmtz.parameterize(d_tb, d_ta));
		
		// All other parameters should be parameterized as usual.
		assertEquals(Collections.singletonMap(new BasicParameter(d_tc, d_ta), -1), pmtz.parameterize(d_ta, d_tc));
		Map<NetworkParameter, Integer> expected = new HashMap<NetworkParameter, Integer>();
		expected.put(new BasicParameter(d_tc, d_tb), -1);
		expected.put(new BasicParameter(d_tc, d_td), 1);
		assertEquals(expected, pmtz.parameterize(d_tb, d_td));
		
		// parameterizeIndirect should give the "as usual" parameterization of the split comparison.
		Map<NetworkParameter, Integer> expected2 = new HashMap<NetworkParameter, Integer>();
		expected2.put(new BasicParameter(d_tc, d_ta), -1);
		expected2.put(new BasicParameter(d_tc, d_tb), 1);
		assertEquals(expected2, pmtz.parameterizeIndirect());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testParameterizeStudy() {
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
		NodeSplitParameterization pmtz = NodeSplitParameterization.create(network, split);
		
		assertEquals(new BasicParameter(d_td, d_ta), pmtz.getParameters().get(0)); // Check assumption
		
		assertEquals(Collections.singletonList(Collections.singletonList(new Pair<Treatment>(d_ta, d_tb))),
				pmtz.parameterizeStudy(s1));
		
		List<Pair<Treatment>> l1 = Arrays.asList(new Pair<Treatment>(d_td, d_ta), new Pair<Treatment>(d_td, d_tb));
		List<Pair<Treatment>> l2 = Arrays.asList(new Pair<Treatment>(d_ta, d_tc));
		List<List<Pair<Treatment>>> expected = Arrays.asList(l1, l2);
		assertEquals(expected, pmtz.parameterizeStudy(s3));
		
		// Check case where only the split node is present
		NodeSplitParameterization pmtz2 = NodeSplitParameterization.create(d_network, new BasicParameter(d_ta, d_tb));
		assertEquals(Collections.singletonList(Collections.singletonList(new Pair<Treatment>(d_ta, d_tb))),
				pmtz2.parameterizeStudy(d_s1));
	}
}
