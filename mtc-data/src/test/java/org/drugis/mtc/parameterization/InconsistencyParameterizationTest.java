package org.drugis.mtc.parameterization;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

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
}
