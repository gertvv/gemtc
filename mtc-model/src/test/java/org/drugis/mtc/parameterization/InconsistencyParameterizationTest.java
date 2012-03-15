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
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.junit.Before;
import org.junit.Test;

import edu.uci.ics.jung.algorithms.transformation.FoldingTransformerFixed.FoldedEdge;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.UndirectedGraph;

public class InconsistencyParameterizationTest {
	private Treatment d_ta;
	private Treatment d_tb;
	private Treatment d_tc;
	private Treatment d_td;
	private Study d_s1;
	private Study d_s2;
	private Study d_s3;
	private Study d_s4;
	private Network d_network;

	@Before
	public void setUp() {
		d_ta = new Treatment("A");
		d_tb = new Treatment("B");
		d_tc = new Treatment("C");
		d_td = new Treatment("D");
		d_s1 = new Study("1");
		d_s1.getMeasurements().add(new Measurement(d_td));
		d_s1.getMeasurements().add(new Measurement(d_tb));
		d_s1.getMeasurements().add(new Measurement(d_tc));
		d_s2 = new Study("2");
		d_s2.getMeasurements().add(new Measurement(d_ta));
		d_s2.getMeasurements().add(new Measurement(d_tb));
		d_s3 = new Study("3");
		d_s3.getMeasurements().add(new Measurement(d_ta));
		d_s3.getMeasurements().add(new Measurement(d_tc));
		d_s4 = new Study("4");
		d_s4.getMeasurements().add(new Measurement(d_ta));
		d_s4.getMeasurements().add(new Measurement(d_td));
		
		d_network = new Network();
		d_network.getTreatments().addAll(Arrays.asList(d_ta, d_tb, d_tc, d_td));
		d_network.getStudies().addAll(Arrays.asList(d_s1, d_s2, d_s3, d_s4));
	}

	@Test
	public void testStandardizeCycle() {
		List<Treatment> cycle = Arrays.asList(d_ta, d_tb, d_tc, d_ta);
		assertEquals(cycle, InconsistencyParameterization.standardizeCycle(cycle));
		assertEquals(cycle, InconsistencyParameterization.standardizeCycle(Arrays.asList(d_tb, d_tc, d_ta, d_tb)));
		assertEquals(cycle, InconsistencyParameterization.standardizeCycle(Arrays.asList(d_ta, d_tc, d_tb, d_ta)));
		assertEquals(cycle, InconsistencyParameterization.standardizeCycle(Arrays.asList(d_tb, d_ta, d_tc, d_tb)));
	}
	
	@Test
	public void testSingleCycleClass() {
		Study s1 = new Study("1");
		s1.getMeasurements().add(new Measurement(d_ta));
		s1.getMeasurements().add(new Measurement(d_tb));
		Study s2 = new Study("2");
		s2.getMeasurements().add(new Measurement(d_ta));
		s2.getMeasurements().add(new Measurement(d_tb));
		s2.getMeasurements().add(new Measurement(d_tc));
		Study s3 = new Study("3");
		s3.getMeasurements().add(new Measurement(d_tb));
		s3.getMeasurements().add(new Measurement(d_tc));
		Network network = new Network();
		network.getTreatments().addAll(Arrays.asList(d_ta, d_tb, d_tc));
		network.getStudies().addAll(Arrays.asList(s1, s2, s3));
		
		UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph = NetworkModel.createComparisonGraph(network);
		Tree<Treatment, FoldedEdge<Treatment, Study>> tree = new DelegateTree<Treatment, FoldedEdge<Treatment, Study>>();
		tree.addVertex(d_ta);
		tree.addEdge(cGraph.findEdge(d_ta, d_tb), d_ta, d_tb);
		tree.addEdge(cGraph.findEdge(d_ta, d_tc), d_ta, d_tc);
		
		Map<Partition, Set<List<Treatment>>> expected = new HashMap<Partition, Set<List<Treatment>>>();
		expected.put(new Partition(Arrays.asList(new Part(d_ta, d_tb, Arrays.asList(s1, s2)), new Part(d_ta, d_tc, Arrays.asList(s2)), new Part(d_tb, d_tc, Arrays.asList(s2, s3)))), 
				Collections.singleton(Arrays.asList(d_ta, d_tb, d_tc, d_ta)));
		Map<Partition, Set<List<Treatment>>> cycleClasses = InconsistencyParameterization.getCycleClasses(cGraph, tree);
		assertEquals(expected, cycleClasses);
		assertEquals(1, InconsistencyParameterization.getInconsistencyDegree(cycleClasses));
		
		assertNotNull(InconsistencyParameterization.findStudyBaselines(d_network.getStudies(), cGraph, cycleClasses));
		assertNotNull(InconsistencyParameterization.findStudyBaselines(d_network.getStudies(), cGraph));
	}
	
	@Test
	public void testConsistencyClass() {
		// create a spanning tree in which BCDB is a cycle (--> not inconsistent)
		UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph = NetworkModel.createComparisonGraph(d_network);
		Tree<Treatment, FoldedEdge<Treatment, Study>> tree = new DelegateTree<Treatment, FoldedEdge<Treatment, Study>>();
		tree.addVertex(d_ta);
		tree.addEdge(cGraph.findEdge(d_ta, d_tb), d_ta, d_tb);
		tree.addEdge(cGraph.findEdge(d_tb, d_tc), d_tb, d_tc);
		tree.addEdge(cGraph.findEdge(d_tb, d_td), d_tb, d_td);
		
		// Expected classes: bcdb=>point, abca=>full, abda=>full
		Map<Partition, Set<List<Treatment>>> expected = new HashMap<Partition, Set<List<Treatment>>>();
		expected.put(new Partition(Arrays.asList(new Part(d_ta, d_tb, Arrays.asList(d_s2)), new Part(d_ta, d_tc, Arrays.asList(d_s3)), new Part(d_tb, d_tc, Arrays.asList(d_s1)))), 
				Collections.singleton(Arrays.asList(d_ta, d_tb, d_tc, d_ta))); // abca
		expected.put(new Partition(Arrays.asList(new Part(d_ta, d_tb, Arrays.asList(d_s2)), new Part(d_ta, d_td, Arrays.asList(d_s4)), new Part(d_tb, d_td, Arrays.asList(d_s1)))), 
				Collections.singleton(Arrays.asList(d_ta, d_tb, d_td, d_ta))); // abda
		expected.put(new Partition(Arrays.asList(new Part(d_tb, d_tb, Arrays.asList(d_s1)))),
				Collections.singleton(Arrays.asList(d_tb, d_tc, d_td, d_tb))); // bcdb
		
		Map<Partition, Set<List<Treatment>>> cycleClasses = InconsistencyParameterization.getCycleClasses(cGraph, tree);
		assertEquals(expected, cycleClasses);
		assertEquals(2, InconsistencyParameterization.getInconsistencyDegree(cycleClasses));
		
		assertNotNull(InconsistencyParameterization.findStudyBaselines(d_network.getStudies(), cGraph, cycleClasses));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testEquivalentClasses() {
		// create a spanning tree in which ACBDA and ACDA are equivalent
		UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph = NetworkModel.createComparisonGraph(d_network);
		Tree<Treatment, FoldedEdge<Treatment, Study>> tree = new DelegateTree<Treatment, FoldedEdge<Treatment, Study>>();
		tree.addVertex(d_ta);
		tree.addEdge(cGraph.findEdge(d_ta, d_tc), d_ta, d_tc);
		tree.addEdge(cGraph.findEdge(d_ta, d_td), d_ta, d_td);
		tree.addEdge(cGraph.findEdge(d_td, d_tb), d_td, d_tb);
		
		// Expected classes: abda=>full, acda=>full, acbda=>acda
		Map<Partition, Set<List<Treatment>>> expected = new HashMap<Partition, Set<List<Treatment>>>();
		expected.put(new Partition(Arrays.asList(new Part(d_ta, d_tb, Arrays.asList(d_s2)), new Part(d_ta, d_td, Arrays.asList(d_s4)), new Part(d_tb, d_td, Arrays.asList(d_s1)))), 
				Collections.singleton(Arrays.asList(d_ta, d_tb, d_td, d_ta))); // abda
		expected.put(new Partition(Arrays.asList(new Part(d_ta, d_tc, Arrays.asList(d_s3)), new Part(d_ta, d_td, Arrays.asList(d_s4)), new Part(d_tc, d_td, Arrays.asList(d_s1)))), 
				new HashSet<List<Treatment>>(Arrays.asList(Arrays.asList(d_ta, d_tc, d_td, d_ta), Arrays.asList(d_ta, d_tc, d_tb, d_td, d_ta)))); // acda, acbda

		Map<Partition, Set<List<Treatment>>> cycleClasses = InconsistencyParameterization.getCycleClasses(cGraph, tree);
		assertEquals(expected, cycleClasses);
		assertEquals(2, InconsistencyParameterization.getInconsistencyDegree(cycleClasses));
		
		assertNotNull(InconsistencyParameterization.findStudyBaselines(d_network.getStudies(), cGraph, cycleClasses));
	}
	
	@Test
	public void testFullInconsistency() {
		// create a spanning tree in which none of the cycles reduce
		UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph = NetworkModel.createComparisonGraph(d_network);
		Tree<Treatment, FoldedEdge<Treatment, Study>> tree = new DelegateTree<Treatment, FoldedEdge<Treatment, Study>>();
		tree.addVertex(d_ta);
		tree.addEdge(cGraph.findEdge(d_ta, d_tb), d_ta, d_tb);
		tree.addEdge(cGraph.findEdge(d_ta, d_tc), d_ta, d_tc);
		tree.addEdge(cGraph.findEdge(d_ta, d_td), d_ta, d_td);
		
		// Expected classes: abca=>full, abda=>full, acda=>full
		Map<Partition, Set<List<Treatment>>> expected = new HashMap<Partition, Set<List<Treatment>>>();
		expected.put(new Partition(Arrays.asList(new Part(d_ta, d_tb, Arrays.asList(d_s2)), new Part(d_ta, d_tc, Arrays.asList(d_s3)), new Part(d_tb, d_tc, Arrays.asList(d_s1)))), 
				Collections.singleton(Arrays.asList(d_ta, d_tb, d_tc, d_ta))); // abca
		expected.put(new Partition(Arrays.asList(new Part(d_ta, d_tb, Arrays.asList(d_s2)), new Part(d_ta, d_td, Arrays.asList(d_s4)), new Part(d_tb, d_td, Arrays.asList(d_s1)))), 
				Collections.singleton(Arrays.asList(d_ta, d_tb, d_td, d_ta))); // abda
		expected.put(new Partition(Arrays.asList(new Part(d_ta, d_tc, Arrays.asList(d_s3)), new Part(d_ta, d_td, Arrays.asList(d_s4)), new Part(d_tc, d_td, Arrays.asList(d_s1)))), 
				Collections.singleton(Arrays.asList(d_ta, d_tc, d_td, d_ta))); // acda
		
		Map<Partition, Set<List<Treatment>>> cycleClasses = InconsistencyParameterization.getCycleClasses(cGraph, tree);
		assertEquals(expected, cycleClasses);
		assertEquals(3, InconsistencyParameterization.getInconsistencyDegree(cycleClasses));
		
		assertNull(InconsistencyParameterization.findStudyBaselines(d_network.getStudies(), cGraph, cycleClasses));
	}

	
	@Test
	public void testFullBaselineAssignment() {
		UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph = NetworkModel.createComparisonGraph(d_network);
		assertNull(InconsistencyParameterization.findStudyBaselines(d_network.getStudies(), cGraph));
	}
	
	@Test
	public void testFindSpanningTree() {
		UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph = NetworkModel.createComparisonGraph(d_network);
		Tree<Treatment, FoldedEdge<Treatment, Study>> tree = InconsistencyParameterization.findSpanningTree(d_network.getStudies(), cGraph);
		Map<Partition, Set<List<Treatment>>> cycleClasses = InconsistencyParameterization.getCycleClasses(cGraph, tree);
		assertEquals(2, InconsistencyParameterization.getInconsistencyDegree(cycleClasses));
	}
	
	@Test
	public void testGetParametersConsistencyClass() {
		// create a spanning tree in which BCDB is a cycle (--> not inconsistent)
		UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph = NetworkModel.createComparisonGraph(d_network);
		Tree<Treatment, FoldedEdge<Treatment, Study>> tree = new DelegateTree<Treatment, FoldedEdge<Treatment, Study>>();
		tree.addVertex(d_ta);
		tree.addEdge(cGraph.findEdge(d_ta, d_tb), d_ta, d_tb);
		tree.addEdge(cGraph.findEdge(d_tb, d_tc), d_tb, d_tc);
		tree.addEdge(cGraph.findEdge(d_tb, d_td), d_tb, d_td);
		
		Map<Partition, Set<List<Treatment>>> cycleClasses = InconsistencyParameterization.getCycleClasses(cGraph, tree);
		Map<Study, Treatment> baselines = InconsistencyParameterization.findStudyBaselines(d_network.getStudies(), cGraph, cycleClasses);
		
		InconsistencyParameterization pmtz = new InconsistencyParameterization(d_network, tree, cycleClasses, baselines);
		
		List<NetworkParameter> expected = Arrays.<NetworkParameter>asList(
				new BasicParameter(d_ta, d_tb), new BasicParameter(d_tb, d_tc), new BasicParameter(d_tb, d_td),
				new InconsistencyParameter(Arrays.asList(d_ta, d_tb, d_tc, d_ta)), 
				new InconsistencyParameter(Arrays.asList(d_ta, d_tb, d_td, d_ta)));
		
		assertEquals(expected, pmtz.getParameters());
	}
	
	@Test
	public void testGetParametersEquivalentClasses() {
		// create a spanning tree in which ACBDA and ACDA are equivalent
		UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph = NetworkModel.createComparisonGraph(d_network);
		Tree<Treatment, FoldedEdge<Treatment, Study>> tree = new DelegateTree<Treatment, FoldedEdge<Treatment, Study>>();
		DirectedGraph<Treatment, FoldedEdge<Treatment, Study>> dg = NetworkModel.toDirected(cGraph);
		tree.addVertex(d_ta);
		tree.addEdge(dg.findEdge(d_ta, d_tc), d_ta, d_tc);
		tree.addEdge(dg.findEdge(d_ta, d_td), d_ta, d_td);
		tree.addEdge(dg.findEdge(d_td, d_tb), d_td, d_tb);
		
		Map<Partition, Set<List<Treatment>>> cycleClasses = InconsistencyParameterization.getCycleClasses(cGraph, tree);
		Map<Study, Treatment> baselines = InconsistencyParameterization.findStudyBaselines(d_network.getStudies(), cGraph, cycleClasses);
		
		InconsistencyParameterization pmtz = new InconsistencyParameterization(d_network, tree, cycleClasses, baselines);
		
		List<NetworkParameter> expected = Arrays.<NetworkParameter>asList(
				new BasicParameter(d_ta, d_tc), new BasicParameter(d_ta, d_td), new BasicParameter(d_td, d_tb),
				new InconsistencyParameter(Arrays.asList(d_ta, d_tb, d_td, d_ta)), 
				new InconsistencyParameter(Arrays.asList(d_ta, d_tc, d_td, d_ta)));
		
		assertEquals(expected, pmtz.getParameters());
	}
	
	@Test
	public void testParameterize() {
		// create a spanning tree in which ACBDA and ACDA are equivalent
		UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph = NetworkModel.createComparisonGraph(d_network);
		Tree<Treatment, FoldedEdge<Treatment, Study>> tree = new DelegateTree<Treatment, FoldedEdge<Treatment, Study>>();
		DirectedGraph<Treatment, FoldedEdge<Treatment, Study>> dg = NetworkModel.toDirected(cGraph);
		tree.addVertex(d_ta);
		tree.addEdge(dg.findEdge(d_ta, d_tc), d_ta, d_tc);
		tree.addEdge(dg.findEdge(d_ta, d_td), d_ta, d_td);
		tree.addEdge(dg.findEdge(d_td, d_tb), d_td, d_tb);
		
		Map<Partition, Set<List<Treatment>>> cycleClasses = InconsistencyParameterization.getCycleClasses(cGraph, tree);
		Map<Study, Treatment> baselines = InconsistencyParameterization.findStudyBaselines(d_network.getStudies(), cGraph, cycleClasses);
		
		InconsistencyParameterization pmtz = new InconsistencyParameterization(d_network, tree, cycleClasses, baselines);

		// Basic parameters
		assertEquals(Collections.singletonMap(new BasicParameter(d_ta, d_tc), 1), pmtz.parameterize(d_ta, d_tc));
		assertEquals(Collections.singletonMap(new BasicParameter(d_ta, d_tc), -1), pmtz.parameterize(d_tc, d_ta));
		assertEquals(Collections.singletonMap(new BasicParameter(d_ta, d_td), 1), pmtz.parameterize(d_ta, d_td));
		assertEquals(Collections.singletonMap(new BasicParameter(d_ta, d_td), -1), pmtz.parameterize(d_td, d_ta));
		assertEquals(Collections.singletonMap(new BasicParameter(d_td, d_tb), -1), pmtz.parameterize(d_tb, d_td));
		assertEquals(Collections.singletonMap(new BasicParameter(d_td, d_tb), 1), pmtz.parameterize(d_td, d_tb));
		
		// Functional parameters
		Map<NetworkParameter, Integer> expected1 = new HashMap<NetworkParameter, Integer>();
		expected1.put(new BasicParameter(d_ta, d_td), 1);
		expected1.put(new BasicParameter(d_td, d_tb), 1);
		expected1.put(new InconsistencyParameter(Arrays.asList(d_ta, d_tb, d_td, d_ta)), 1);
		assertEquals(expected1, pmtz.parameterize(d_ta, d_tb));
		
		Map<NetworkParameter, Integer> expected2 = new HashMap<NetworkParameter, Integer>();
		expected2.put(new BasicParameter(d_ta, d_tc), -1);
		expected2.put(new BasicParameter(d_ta, d_td), 1);
		expected2.put(new InconsistencyParameter(Arrays.asList(d_ta, d_tc, d_td, d_ta)), 1);
		assertEquals(expected2, pmtz.parameterize(d_tc, d_td));
		
		Map<NetworkParameter, Integer> expected3 = new HashMap<NetworkParameter, Integer>();
		expected3.put(new BasicParameter(d_ta, d_td), -1);
		expected3.put(new BasicParameter(d_td, d_tb), -1);
		expected3.put(new BasicParameter(d_ta, d_tc), 1);
		expected3.put(new InconsistencyParameter(Arrays.asList(d_ta, d_tc, d_td, d_ta)), -1);
		assertEquals(expected3, pmtz.parameterize(d_tb, d_tc));
	}
	
	
	@Test
	public void testParameterizeConsistencyClass() {
		// create a spanning tree in which BCDB is a cycle (--> not inconsistent)
		UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph = NetworkModel.createComparisonGraph(d_network);
		Tree<Treatment, FoldedEdge<Treatment, Study>> tree = new DelegateTree<Treatment, FoldedEdge<Treatment, Study>>();
		tree.addVertex(d_ta);
		tree.addEdge(cGraph.findEdge(d_ta, d_tb), d_ta, d_tb);
		tree.addEdge(cGraph.findEdge(d_tb, d_tc), d_tb, d_tc);
		tree.addEdge(cGraph.findEdge(d_tb, d_td), d_tb, d_td);
		
		Map<Partition, Set<List<Treatment>>> cycleClasses = InconsistencyParameterization.getCycleClasses(cGraph, tree);
		Map<Study, Treatment> baselines = InconsistencyParameterization.findStudyBaselines(d_network.getStudies(), cGraph, cycleClasses);
		
		InconsistencyParameterization pmtz = new InconsistencyParameterization(d_network, tree, cycleClasses, baselines);
		
		// Functional parameters
		Map<NetworkParameter, Integer> expected1 = new HashMap<NetworkParameter, Integer>();
		expected1.put(new BasicParameter(d_tb, d_td), 1);
		expected1.put(new BasicParameter(d_tb, d_tc), -1);
		assertEquals(expected1, pmtz.parameterize(d_tc, d_td));
	}
	
	@Test
	public void testParameterizeNonExistantCycle() {
		Study s1 = new Study("1");
		s1.getMeasurements().add(new Measurement(d_td));
		s1.getMeasurements().add(new Measurement(d_tb));
		s1.getMeasurements().add(new Measurement(d_tc));
		Study s2 = new Study("2");
		s2.getMeasurements().add(new Measurement(d_ta));
		s2.getMeasurements().add(new Measurement(d_tc));
		Study s3 = new Study("3");
		s3.getMeasurements().add(new Measurement(d_ta));
		s3.getMeasurements().add(new Measurement(d_td));
		Network network = new Network();
		network.getTreatments().addAll(Arrays.asList(d_ta, d_tb, d_tc, d_td));
		network.getStudies().addAll(Arrays.asList(s1, s2, s3));
		
		UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph = NetworkModel.createComparisonGraph(network);
		Tree<Treatment, FoldedEdge<Treatment, Study>> tree = new DelegateTree<Treatment, FoldedEdge<Treatment, Study>>();
		DirectedGraph<Treatment, FoldedEdge<Treatment, Study>> dg = NetworkModel.toDirected(cGraph);
		tree.addVertex(d_ta);
		tree.addEdge(dg.findEdge(d_ta, d_tc), d_ta, d_tc);
		tree.addEdge(dg.findEdge(d_ta, d_td), d_ta, d_td);
		tree.addEdge(dg.findEdge(d_td, d_tb), d_td, d_tb);
		
		Map<Partition, Set<List<Treatment>>> cycleClasses = InconsistencyParameterization.getCycleClasses(cGraph, tree);
		Map<Study, Treatment> baselines = InconsistencyParameterization.findStudyBaselines(d_network.getStudies(), cGraph, cycleClasses);
		
		InconsistencyParameterization pmtz = new InconsistencyParameterization(d_network, tree, cycleClasses, baselines);
		
		// Functional parameters
		Map<NetworkParameter, Integer> expected1 = new HashMap<NetworkParameter, Integer>();
		expected1.put(new BasicParameter(d_ta, d_td), 1);
		expected1.put(new BasicParameter(d_td, d_tb), 1);
		assertEquals(expected1, pmtz.parameterize(d_ta, d_tb));
	}
}
