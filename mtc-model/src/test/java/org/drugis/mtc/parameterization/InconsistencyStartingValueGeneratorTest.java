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
		
		StartingValueGenerator generator = new ContinuousDataStartingValueGenerator(network);
		
		Map<BasicParameter, Double> basicStart = new HashMap<BasicParameter, Double>();
		final BasicParameter pab = new BasicParameter(ta, tb);
		basicStart.put(pab, generator.getRelativeEffect(pab));
		final BasicParameter pac = new BasicParameter(ta, tc);
		basicStart.put(pac, generator.getRelativeEffect(pac));

		InconsistencyParameter incons = (InconsistencyParameter) pmtz.getParameters().get(2);
		assertEquals(0.48, InconsistencyStartingValueGenerator.generate(incons, pmtz, generator, basicStart), EPSILON);
	}
}
