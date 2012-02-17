package org.drugis.mtc.parameterization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
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

public class InconsistencyBaselineSearchProblemTest {
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
	public void testInitialState() {
		InconsistencyBaselineSearchProblem problem = new InconsistencyBaselineSearchProblem(d_network.getStudies(), NetworkModel.createComparisonGraph(d_network));
		
		Map<Study, Treatment> expected = new HashMap<Study, Treatment>();
		expected.put(d_s2, d_ta);
		expected.put(d_s3, d_ta);
		expected.put(d_s4, d_ta);
		
		assertEquals(expected, problem.getInitialState());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSuccessors() {
		InconsistencyBaselineSearchProblem problem = new InconsistencyBaselineSearchProblem(d_network.getStudies(), NetworkModel.createComparisonGraph(d_network));
		
		Map<Study, Treatment> initial = problem.getInitialState();
		
		Map<Study, Treatment> expected1 = new HashMap<Study, Treatment>(initial);
		expected1.put(d_s1, d_tb);
		Map<Study, Treatment> expected2 = new HashMap<Study, Treatment>(initial);
		expected2.put(d_s1, d_tc);
		Map<Study, Treatment> expected3 = new HashMap<Study, Treatment>(initial);
		expected3.put(d_s1, d_td);
		List<Map<Study, Treatment>> expected = Arrays.asList(expected1, expected2, expected3);
		
		assertEquals(expected, problem.getSuccessors(initial));
		
		// Also test with additional study
		Study s5 = new Study("5");
		s5.getMeasurements().add(new Measurement(d_ta));
		s5.getMeasurements().add(new Measurement(d_tb));
		s5.getMeasurements().add(new Measurement(d_tc));
		d_network.getStudies().add(s5);
		
		InconsistencyBaselineSearchProblem problem2 = new InconsistencyBaselineSearchProblem(d_network.getStudies(), NetworkModel.createComparisonGraph(d_network));
		
		assertEquals(expected, problem2.getSuccessors(problem2.getInitialState()));
		
		Map<Study, Treatment> someSucc = problem2.getSuccessors(problem2.getInitialState()).get(0);
		assertTrue(problem2.getSuccessors(someSucc).get(0).containsKey(s5));
	}
	
	@Test
	public void testFullBaselineGoalTest() {
		InconsistencyBaselineSearchProblem problem = new InconsistencyBaselineSearchProblem(d_network.getStudies(), NetworkModel.createComparisonGraph(d_network));

		assertFalse(problem.isGoal(problem.getInitialState()));
		assertFalse(problem.isGoal(problem.getSuccessors(problem.getInitialState()).get(0)));
		assertFalse(problem.isGoal(problem.getSuccessors(problem.getInitialState()).get(1)));
		assertFalse(problem.isGoal(problem.getSuccessors(problem.getInitialState()).get(2)));
		
		// Remove the problematic study
		d_network.getStudies().remove(d_s1);
		InconsistencyBaselineSearchProblem problem2 = new InconsistencyBaselineSearchProblem(d_network.getStudies(), NetworkModel.createComparisonGraph(d_network));

		assertTrue(problem2.isGoal(problem2.getInitialState()));
	}
	
	@Test
	public void testRedundantCycleClassBaselineGoalTest() {
		// create a spanning tree in which ACBDA and ACDA are equivalent
		UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph = NetworkModel.createComparisonGraph(d_network);
		Tree<Treatment, FoldedEdge<Treatment, Study>> tree = new DelegateTree<Treatment, FoldedEdge<Treatment, Study>>();
		tree.addVertex(d_ta);
		tree.addEdge(cGraph.findEdge(d_ta, d_tc), d_ta, d_tc);
		tree.addEdge(cGraph.findEdge(d_ta, d_td), d_ta, d_td);
		tree.addEdge(cGraph.findEdge(d_td, d_tb), d_td, d_tb);
		Map<Partition, Set<List<Treatment>>> cycleClasses = InconsistencyParameterization.getCycleClasses(cGraph, tree);
		
		InconsistencyBaselineSearchProblem problem = new InconsistencyBaselineSearchProblem(d_network.getStudies(), cGraph, cycleClasses);

		List<Map<Study, Treatment>> successors = problem.getSuccessors(problem.getInitialState());
		assertTrue(problem.isGoal(successors.get(0))); // B baseline for s1
		assertFalse(problem.isGoal(successors.get(1))); // C baseline for s1
		assertTrue(problem.isGoal(successors.get(2))); // D baseline for s1
	}
	
	@Test
	public void testPointCycleClassBaselineGoalTest() {
		// create a spanning tree in which BCDB is a cycle (--> not inconsistent)
		UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph = NetworkModel.createComparisonGraph(d_network);
		Tree<Treatment, FoldedEdge<Treatment, Study>> tree = new DelegateTree<Treatment, FoldedEdge<Treatment, Study>>();
		tree.addVertex(d_ta);
		tree.addEdge(cGraph.findEdge(d_ta, d_tb), d_ta, d_tb);
		tree.addEdge(cGraph.findEdge(d_tb, d_tc), d_tb, d_tc);
		tree.addEdge(cGraph.findEdge(d_tb, d_td), d_tb, d_td);
		Map<Partition, Set<List<Treatment>>> cycleClasses = InconsistencyParameterization.getCycleClasses(cGraph, tree);
		
		InconsistencyBaselineSearchProblem problem = new InconsistencyBaselineSearchProblem(d_network.getStudies(), cGraph, cycleClasses);

		List<Map<Study, Treatment>> successors = problem.getSuccessors(problem.getInitialState());
		assertTrue(problem.isGoal(successors.get(0))); // B baseline for s1
		assertFalse(problem.isGoal(successors.get(1))); // C baseline for s1
		assertFalse(problem.isGoal(successors.get(2))); // D baseline for s1
	}
	
}
