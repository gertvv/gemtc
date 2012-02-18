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

package org.drugis.mtc.jags;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.drugis.common.ResourceUtil;
import org.drugis.mtc.model.JAXBHandler;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.parameterization.ConsistencyParameterization;
import org.drugis.mtc.parameterization.InconsistencyParameterization;
import org.drugis.mtc.parameterization.NetworkModel;
import org.drugis.mtc.parameterization.NodeSplitParameterization;
import org.drugis.mtc.parameterization.Partition;
import org.drugis.mtc.parameterization.PriorGenerator;
import org.drugis.mtc.parameterization.PriorStartingValueGenerator;
import org.junit.Before;
import org.junit.Test;

import edu.uci.ics.jung.algorithms.transformation.FoldingTransformerFixed.FoldedEdge;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.UndirectedGraph;

public class JagsSyntaxModelTest {
	private Network d_n1;
	private Treatment d_ta;
	private Treatment d_tb;
	private Treatment d_tc;
	private Map<Study, Treatment> d_n1_baselines;
	private UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> d_n1_cGraph;
	private Tree<Treatment, FoldedEdge<Treatment, Study>> d_n1_tree;
	private Network d_n2;
	private UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> d_n2_cGraph;
	private DelegateTree<Treatment, FoldedEdge<Treatment, Study>> d_n2_tree;
	private HashMap<Study, Treatment> d_n2_baselines;

	@Test
	public void testWriteInt() {
		assertEquals("3L", JagsSyntaxModel.writeNumber(3, true));
		assertEquals("15L", JagsSyntaxModel.writeNumber(15, true));
	}

	@Test
	public void testWriteFloat() {
		assertEquals("3.0", JagsSyntaxModel.writeNumber(3.0, true));
		assertEquals("15.0", JagsSyntaxModel.writeNumber(15.0, true));
	}

	@Test 
	public void testIntMatrixColMajor() {
		Integer m[][] = { {1, 2, 3, 4}, {5, 6, 7, 8} };
		assertEquals("structure(c(1L, 5L, 2L, 6L, 3L, 7L, 4L, 8L), .Dim = c(2L, 4L))", JagsSyntaxModel.writeMatrix(m, true));
	}
	
	@Test 
	public void testIntMatrixRowMajor() {
		Integer m[][] = { {1, 2, 3, 4}, {5, 6, 7, 8} };
		assertEquals("structure(.Data = c(1, 2, 3, 4, 5, 6, 7, 8), .Dim = c(2, 4))", JagsSyntaxModel.writeMatrix(m, false));
	}
	
	private static String read(String path) throws IOException {
		return ResourceUtil.read(JagsSyntaxModel.class, path);
	}
	
	@Before
	public void setUp() throws JAXBException {
		d_ta = new Treatment("A");
		d_tb = new Treatment("B");
		d_tc = new Treatment("C");
		
		InputStream is1 = JagsSyntaxModelTest.class.getResourceAsStream("network1.xml");
		d_n1 = JAXBHandler.readNetwork(is1);
		
		d_n1_cGraph = NetworkModel.createComparisonGraph(d_n1);
		d_n1_tree = new DelegateTree<Treatment, FoldedEdge<Treatment,Study>>();
		d_n1_tree.addVertex(d_ta);
		d_n1_tree.addEdge(d_n1_cGraph.findEdge(d_ta, d_tb), d_ta, d_tb);
		d_n1_tree.addEdge(d_n1_cGraph.findEdge(d_tb, d_tc), d_tb, d_tc);
		
		d_n1_baselines = new HashMap<Study, Treatment>();
		d_n1_baselines.put(d_n1.getStudies().get(0), d_tb);
		d_n1_baselines.put(d_n1.getStudies().get(1), d_ta);
		d_n1_baselines.put(d_n1.getStudies().get(2), d_ta);
		
		
		InputStream is2 = JagsSyntaxModelTest.class.getResourceAsStream("network2.xml");
		d_n2 = JAXBHandler.readNetwork(is2);
		
		d_n2_cGraph = NetworkModel.createComparisonGraph(d_n2);
		d_n2_tree = new DelegateTree<Treatment, FoldedEdge<Treatment,Study>>();
		d_n2_tree.addVertex(d_ta);
		d_n2_tree.addEdge(d_n2_cGraph.findEdge(d_ta, d_tb), d_ta, d_tb);
		d_n2_tree.addEdge(d_n2_cGraph.findEdge(d_tb, d_tc), d_tb, d_tc);
		
		d_n2_baselines = new HashMap<Study, Treatment>();
		d_n2_baselines.put(d_n2.getStudies().get(0), d_tb);
		d_n2_baselines.put(d_n2.getStudies().get(1), d_ta);
		d_n2_baselines.put(d_n2.getStudies().get(2), d_ta);
	}
	
	@Test
	public void testDichotomousInconsistency() throws IOException {
		final Map<Partition, Set<List<Treatment>>> cycleClasses = InconsistencyParameterization.getCycleClasses(d_n1_cGraph, d_n1_tree);
		final InconsistencyParameterization pmtz = new InconsistencyParameterization(d_n1, d_n1_tree, cycleClasses, d_n1_baselines);
		JagsSyntaxModel model = new JagsSyntaxModel(d_n1, pmtz, true);
		
		assertEquals(read("data-inco-dich.txt"), model.dataText());
		assertEquals(read("model-inco-dich.txt"), model.modelText());
		assertEquals(read("script-inco-dich.txt"), model.scriptText("jags", 3, 30000, 20000));
		assertEquals(read("analysis-inco-dich.txt"), model.analysisText("jags"));
		// FIXME: add initial values test?
	}
	
	@Test
	public void testDichotomousConsistency() throws IOException {
		final ConsistencyParameterization pmtz = new ConsistencyParameterization(d_n1, d_n1_tree, d_n1_baselines);
		JagsSyntaxModel model = new JagsSyntaxModel(d_n1, pmtz, true);
		
		assertEquals(read("data-cons-dich.txt"), model.dataText());
		assertEquals(read("model-cons-dich.txt"), model.modelText());
		assertEquals(read("script-cons-dich.txt"), model.scriptText("jags", 3, 30000, 20000));
		assertEquals(read("analysis-cons-dich.txt"), model.analysisText("jags"));
		assertEquals(read("init-cons-dich.txt"), model.initialValuesText(new PriorStartingValueGenerator(new PriorGenerator(d_n1))));
	}
	
	@Test
	public void testDichotomousNodeSplit() throws IOException {
		Tree<Treatment, FoldedEdge<Treatment, Study>> tree = new DelegateTree<Treatment, FoldedEdge<Treatment,Study>>();
		tree.addVertex(d_ta);
		tree.addEdge(d_n1_cGraph.findEdge(d_ta, d_tb), d_ta, d_tb);
		tree.addEdge(d_n1_cGraph.findEdge(d_ta, d_tc), d_ta, d_tc);
		
		Map<Study, Treatment> baselines = new HashMap<Study, Treatment>();
		baselines.put(d_n1.getStudies().get(0), d_ta);
		baselines.put(d_n1.getStudies().get(1), d_ta);
		baselines.put(d_n1.getStudies().get(2), d_ta);
		
		final NodeSplitParameterization pmtz = new NodeSplitParameterization(d_n1, new BasicParameter(d_tb, d_tc), tree, baselines);
		JagsSyntaxModel model = new JagsSyntaxModel(d_n1, pmtz, true);
		assertEquals(read("data-splt-dich.txt"), model.dataText());
		assertEquals(read("model-splt-dich.txt"), model.modelText());
		assertEquals(read("script-splt-dich.txt"), model.scriptText("jags", 3, 30000, 20000));
		// FIXME: implement and test analysisText
		assertEquals(read("init-splt-dich.txt"), model.initialValuesText(new PriorStartingValueGenerator(new PriorGenerator(d_n1))));
	}
	
	@Test
	public void testContinuousConsistency() throws IOException {
		final ConsistencyParameterization pmtz = new ConsistencyParameterization(d_n2, d_n2_tree, d_n2_baselines);
		JagsSyntaxModel model = new JagsSyntaxModel(d_n2, pmtz, true);
		
		assertEquals(read("data-inco-cont.txt"), model.dataText());
		assertEquals(read("model-cons-cont.txt"), model.modelText());
		assertEquals(read("script-cons-cont.txt"), model.scriptText("jags", 3, 30000, 20000));
		assertEquals(read("analysis-cons-cont.txt"), model.analysisText("jags"));
		// FIXME: add initial values test?
	}
	
	@Test
	public void testContinuousInconsistency() throws IOException {
		final Map<Partition, Set<List<Treatment>>> cycleClasses = InconsistencyParameterization.getCycleClasses(d_n2_cGraph, d_n2_tree);
		final InconsistencyParameterization pmtz = new InconsistencyParameterization(d_n2, d_n2_tree, cycleClasses, d_n2_baselines);
		JagsSyntaxModel model = new JagsSyntaxModel(d_n2, pmtz, true);
		
		assertEquals(read("data-inco-cont.txt"), model.dataText());
		assertEquals(read("model-inco-cont.txt"), model.modelText());
		// FIXME: add script test?
		// FIXME: add analysis test?
		// FIXME: add initial values test?
	}
}
