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
import org.drugis.mtc.parameterization.InconsistencyParameterization;
import org.drugis.mtc.parameterization.NetworkModel;
import org.drugis.mtc.parameterization.Partition;
import org.junit.Test;

import edu.uci.ics.jung.algorithms.transformation.FoldingTransformerFixed.FoldedEdge;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.UndirectedGraph;

public class JagsSyntaxModelTest {
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
	
	@Test
	public void testDichotomousInconsistency() throws JAXBException, IOException {
		InputStream is = JagsSyntaxModelTest.class.getResourceAsStream("network1.xml");
		Network network = JAXBHandler.readNetwork(is);
		
		Treatment ta = network.getTreatments().get(0);
		Treatment tb = network.getTreatments().get(1);
		Treatment tc = network.getTreatments().get(2);
		
		UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph = NetworkModel.createComparisonGraph(network);
		// Fix the tree to AB, BC.
		Tree<Treatment, FoldedEdge<Treatment, Study>> tree = new DelegateTree<Treatment, FoldedEdge<Treatment,Study>>();
		tree.addVertex(ta);
		tree.addEdge(cGraph.findEdge(ta, tb), ta, tb);
		tree.addEdge(cGraph.findEdge(tb, tc), tb, tc);
		final Map<Partition, Set<List<Treatment>>> cycleClasses = InconsistencyParameterization.getCycleClasses(cGraph, tree);
		Map<Study, Treatment> baselines = new HashMap<Study, Treatment>();
		baselines.put(network.getStudies().get(0), tb);
		baselines.put(network.getStudies().get(1), ta);
		baselines.put(network.getStudies().get(2), ta);
		final InconsistencyParameterization pmtz = new InconsistencyParameterization(network, tree, cycleClasses, baselines);
		JagsSyntaxModel model = new JagsSyntaxModel(network, pmtz, true);
		
		assertEquals(read("data-inco-dich.txt"), model.dataText());
		assertEquals(read("model-inco-dich.txt"), model.modelText());
		// FIXME: add initial values test?
		assertEquals(read("script-inco-dich.txt"), model.scriptText("jags", 3, 30000, 20000));
		assertEquals(read("analysis-inco-dich.txt"), model.analysisText("jags"));
	}
}
