package org.drugis.mtc.jags;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.drugis.mtc.model.JAXBHandler;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.ConsistencyParameterization;
import org.drugis.mtc.parameterization.InconsistencyParameterization;
import org.drugis.mtc.parameterization.NetworkModel;
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
	private Treatment d_n1_ta;
	private Treatment d_n1_tb;
	private Treatment d_n1_tc;
	private Map<Study, Treatment> d_n1_baselines;
	private UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> d_n1_cGraph;
	private Tree<Treatment, FoldedEdge<Treatment, Study>> d_n1_tree;

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
		InputStream is = JagsSyntaxModelTest.class.getResourceAsStream(path);
		StringBuilder str = new StringBuilder();
		Reader reader = new InputStreamReader(is, "UTF-8");
		char[] buffer = new char[2048];
		int read;
		while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
			str.append(buffer, 0, read);
		}
		reader.close();
		return str.toString();
	}
	
	@Before
	public void setUp() throws JAXBException {
		InputStream is = JagsSyntaxModelTest.class.getResourceAsStream("network1.xml");
		d_n1 = JAXBHandler.readNetwork(is);
		
		d_n1_ta = d_n1.getTreatments().get(0);
		d_n1_tb = d_n1.getTreatments().get(1);
		d_n1_tc = d_n1.getTreatments().get(2);

		d_n1_cGraph = NetworkModel.createComparisonGraph(d_n1);
		d_n1_tree = new DelegateTree<Treatment, FoldedEdge<Treatment,Study>>();
		d_n1_tree.addVertex(d_n1_ta);
		d_n1_tree.addEdge(d_n1_cGraph.findEdge(d_n1_ta, d_n1_tb), d_n1_ta, d_n1_tb);
		d_n1_tree.addEdge(d_n1_cGraph.findEdge(d_n1_tb, d_n1_tc), d_n1_tb, d_n1_tc);
		
		d_n1_baselines = new HashMap<Study, Treatment>();
		d_n1_baselines.put(d_n1.getStudies().get(0), d_n1_tb);
		d_n1_baselines.put(d_n1.getStudies().get(1), d_n1_ta);
		d_n1_baselines.put(d_n1.getStudies().get(2), d_n1_ta);
	}
	
	@Test
	public void testDichotomousInconsistency() throws IOException {
		final Map<Partition, Set<List<Treatment>>> cycleClasses = InconsistencyParameterization.getCycleClasses(d_n1_cGraph, d_n1_tree);
		final InconsistencyParameterization pmtz = new InconsistencyParameterization(d_n1, d_n1_tree, cycleClasses, d_n1_baselines);
		JagsSyntaxModel model = new JagsSyntaxModel(d_n1, pmtz, true);
		
		assertEquals(read("data-inco-dich.txt"), model.dataText());
		assertEquals(read("model-inco-dich.txt"), model.modelText());
		// FIXME: add initial values test?
		assertEquals(read("script-inco-dich.txt"), model.scriptText("jags", 3, 30000, 20000));
		assertEquals(read("analysis-inco-dich.txt"), model.analysisText("jags"));
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
}
