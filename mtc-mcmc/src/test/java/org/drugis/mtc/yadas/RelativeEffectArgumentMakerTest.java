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

package org.drugis.mtc.yadas;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.ConsistencyParameterization;
import org.drugis.mtc.parameterization.InconsistencyParameterization;
import org.drugis.mtc.parameterization.NetworkModel;
import org.drugis.mtc.parameterization.Partition;
import org.junit.Before;
import org.junit.Test;

import edu.uci.ics.jung.algorithms.transformation.FoldingTransformerFixed.FoldedEdge;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.UndirectedGraph;

public class RelativeEffectArgumentMakerTest {

	private static final double EPSILON = 0.0000001;
	private Treatment d_ta;
	private Treatment d_tb;
	private Treatment d_tc;
	private Treatment d_td;
	private Study d_s1;
	private Study d_s2;
	private Study d_s3;
	private Study d_s4;
	private Network d_network;
	private InconsistencyParameterization d_incoPmtz;
	private ConsistencyParameterization d_consPmtz;

	@Before
	public void setUp() {
		d_ta = new Treatment("A");
		d_tb = new Treatment("B");
		d_tc = new Treatment("C");
		d_td = new Treatment("D");
		
		d_s1 = new Study("1");
		d_s1.getMeasurements().addAll(Arrays.asList(new Measurement(d_ta), new Measurement(d_tb)));
		d_s2 = new Study("2");
		d_s2.getMeasurements().addAll(Arrays.asList(new Measurement(d_tb), new Measurement(d_tc), new Measurement(d_td)));
		d_s3 = new Study("3");
		d_s3.getMeasurements().addAll(Arrays.asList(new Measurement(d_ta), new Measurement(d_td)));
		d_s4 = new Study("4");
		d_s4.getMeasurements().addAll(Arrays.asList(new Measurement(d_tc), new Measurement(d_td)));
		
		d_network = new Network();
		d_network.getTreatments().addAll(Arrays.asList(d_ta, d_tb, d_tc, d_td));
		d_network.getStudies().addAll(Arrays.asList(d_s1, d_s2, d_s3, d_s4));
		
		UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph = NetworkModel.createComparisonGraph(d_network);
		Tree<Treatment, FoldedEdge<Treatment, Study>> tree = new DelegateTree<Treatment, FoldedEdge<Treatment,Study>>();
		tree.addVertex(d_ta);
		tree.addEdge(cGraph.findEdge(d_ta, d_tb), d_ta, d_tb);
		tree.addEdge(cGraph.findEdge(d_tb, d_tc), d_tb, d_tc);
		tree.addEdge(cGraph.findEdge(d_ta, d_td), d_ta, d_td);
		
		Map<Partition, Set<List<Treatment>>> cycleClasses = InconsistencyParameterization.getCycleClasses(cGraph, tree);
		Map<Study, Treatment> baselines = InconsistencyParameterization.findStudyBaselines(d_network.getStudies(), cGraph, cycleClasses);
		d_incoPmtz = new InconsistencyParameterization(d_network, tree, cycleClasses, baselines);
		d_consPmtz = new ConsistencyParameterization(d_network, tree, baselines);
	}

	@Test
	public void testGetArgumentConsistency() {
		double[] input = new double[] {-1.0, 1.0, 3.0};
		double[] expected1 = new double[] {-1.0};
		double[] expected2 = new double[] {3.0, 2.0};
		double[] expected3 = new double[] {1.0};
		double[] expected4 = new double[] {-1.0};

		RelativeEffectArgumentMaker maker1a = new RelativeEffectArgumentMaker(d_consPmtz, d_s1, 0, -1);
		assertArrayEquals(expected1, maker1a.getArgument(new double[][] {input}), EPSILON);

		RelativeEffectArgumentMaker maker1b = new RelativeEffectArgumentMaker(d_consPmtz, d_s1, 1, -1);
		assertArrayEquals(expected1, maker1b.getArgument(new double[][] {null, input}), EPSILON);

		RelativeEffectArgumentMaker maker2 = new RelativeEffectArgumentMaker(d_consPmtz, d_s2, 0, -1);
		assertArrayEquals(expected2, maker2.getArgument(new double[][] {input}), EPSILON);

		RelativeEffectArgumentMaker maker3 = new RelativeEffectArgumentMaker(d_consPmtz, d_s3, 0, -1);
		assertArrayEquals(expected3, maker3.getArgument(new double[][] {input}), EPSILON);

		RelativeEffectArgumentMaker maker4 = new RelativeEffectArgumentMaker(d_consPmtz, d_s4, 0, -1);
		assertArrayEquals(expected4, maker4.getArgument(new double[][] {input}), EPSILON);
	}
	
	@Test
	public void testGetArgumentInconsistency() {
		double[] basic = new double[] {-1.0, 1.0, 3.0}; // AB, AD, BC
		double[] incons = new double[] {-0.5, 0.5}; // ABCDA, ABDA
		double[] expected1 = new double[] {-1.0};
		double[] expected2 = new double[] {3.0, 2.5};
		double[] expected3 = new double[] {1.0};
		double[] expected4 = new double[] {-1.5};

		RelativeEffectArgumentMaker maker1a = new RelativeEffectArgumentMaker(d_incoPmtz, d_s1, 0, 1);
		assertArrayEquals(expected1, maker1a.getArgument(new double[][] {basic, incons}), EPSILON);

		RelativeEffectArgumentMaker maker1b = new RelativeEffectArgumentMaker(d_incoPmtz, d_s1, 2, 1);
		assertArrayEquals(expected1, maker1b.getArgument(new double[][] {null, incons, basic}), EPSILON);

		RelativeEffectArgumentMaker maker2 = new RelativeEffectArgumentMaker(d_incoPmtz, d_s2, 0, 1);
		assertArrayEquals(expected2, maker2.getArgument(new double[][] {basic, incons}), EPSILON);

		RelativeEffectArgumentMaker maker3 = new RelativeEffectArgumentMaker(d_incoPmtz, d_s3, 0, 1);
		assertArrayEquals(expected3, maker3.getArgument(new double[][] {basic, incons}), EPSILON);

		RelativeEffectArgumentMaker maker4 = new RelativeEffectArgumentMaker(d_incoPmtz, d_s4, 0, 1);
		assertArrayEquals(expected4, maker4.getArgument(new double[][] {basic, incons}), EPSILON);
	}

	@Test
	public void testGetArgumentInconsistencyNone() {
		// Created because creating an inconsistency model with no inconsistencies threw an exception.
		Network network = new Network();
		network.getTreatments().addAll(d_s1.getTreatments());
		network.getStudies().add(d_s1);
		
		InconsistencyParameterization pmtz = InconsistencyParameterization.create(network);

		double[] basic = new double[] {-1.0};
		double[] incons = new double[] {};

		RelativeEffectArgumentMaker maker0 = new RelativeEffectArgumentMaker(pmtz, d_s1, 0, 1);
		assertArrayEquals(basic, maker0.getArgument(new double[][] {basic, incons}), EPSILON);
	}
}