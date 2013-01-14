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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.drugis.common.stat.EstimateWithPrecision;
import org.drugis.common.stat.Statistics;
import org.drugis.mtc.data.DataType;
import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.util.DerSimonianLairdPooling;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class ContinuousDataStartingValueGeneratorTest {
	private static final double EPSILON = 0.0000001;
	Treatment d_ta;
	Treatment d_tb;
	Treatment d_tc;
	Study d_s1;
	Study d_s2;
	Study d_s3;
	Study d_s4;
	Network d_network;
	PriorGenerator d_priorGen;
	
	@Before
	public void setUp() {
		d_ta = new Treatment("A");
		d_tb = new Treatment("B");
		d_tc = new Treatment("C");
		d_s1 = new Study("1");
		d_s1.getMeasurements().add(new Measurement(d_tb, -2.5, 1.6, 177));
		d_s1.getMeasurements().add(new Measurement(d_tc, -2.6, 1.5, 176));
		d_s2 = new Study("2");
		d_s2.getMeasurements().add(new Measurement(d_ta, -1.93, 1.22, 30));
		d_s2.getMeasurements().add(new Measurement(d_tb, -1.52, 1.18, 30));
		d_s3 = new Study("3");
		d_s3.getMeasurements().add(new Measurement(d_ta, -2.01, 1.5, 35));
		d_s3.getMeasurements().add(new Measurement(d_tb, -1.73, 1.45, 39));
		d_s4 = new Study("4");
		d_s4.getMeasurements().add(new Measurement(d_ta, -1.35, 1.23, 49));
		d_s4.getMeasurements().add(new Measurement(d_tc, -1.52, 0.96, 46));

		d_network = new Network();
		d_network.setType(DataType.CONTINUOUS);
		d_network.getTreatments().addAll(Arrays.asList(d_ta, d_tb, d_tc));
		d_network.getStudies().addAll(Arrays.asList(d_s1, d_s2, d_s3, d_s4));
		
		d_priorGen = new PriorGenerator(d_network);
	}
		
	private RandomGenerator mockRandom(double value) {
		RandomGenerator rng = EasyMock.createMock(RandomGenerator.class);
		EasyMock.expect(rng.nextGaussian()).andReturn(value);
		EasyMock.replay(rng);
		return rng;
	}

	@Test
	public void testGenerateTreatmentEffect() {
		ContinuousDataStartingValueGenerator generator = new ContinuousDataStartingValueGenerator(d_network);
		
		Measurement m0 = NetworkModel.findMeasurement(d_s1, d_tb);
		assertEquals(m0.getMean(), generator.getTreatmentEffect(d_s1, d_tb), EPSILON);
		
		Measurement m1 = NetworkModel.findMeasurement(d_s2, d_ta);
		assertEquals(m1.getMean(), generator.getTreatmentEffect(d_s2, d_ta), EPSILON);
	}
	

	@Test 
	public void testRandomizedTreatmentEffect1() {
		RandomGenerator rng = mockRandom(1.0);
		StartingValueGenerator generator = new ContinuousDataStartingValueGenerator(d_network, rng, 1.0);

		Measurement m0 = NetworkModel.findMeasurement(d_s1, d_tb);

		assertEquals(m0.getMean() + m0.getStdDev() / Math.sqrt(m0.getSampleSize()), generator.getTreatmentEffect(d_s1, d_tb), EPSILON);
		EasyMock.verify(rng);
	}
	
	@Test 
	public void testRandomizedTreatmentEffect2() {
		RandomGenerator rng = mockRandom(0.23);
		StartingValueGenerator generator = new ContinuousDataStartingValueGenerator(d_network, rng, 2.0);

		Measurement m0 = NetworkModel.findMeasurement(d_s2, d_tb);

		assertEquals(m0.getMean() + 0.46 * m0.getStdDev() / Math.sqrt(m0.getSampleSize()), generator.getTreatmentEffect(d_s2, d_tb), EPSILON);
		EasyMock.verify(rng);
	}
	
	@Test
	public void testGenerateStudyRelativeEffect() {
		ContinuousDataStartingValueGenerator generator = new ContinuousDataStartingValueGenerator(d_network);

		Measurement m0 = NetworkModel.findMeasurement(d_s2, d_ta);
		Measurement m1 = NetworkModel.findMeasurement(d_s2, d_tb);
		assertEquals(m1.getMean() - m0.getMean(), generator.getRelativeEffect(d_s2, new BasicParameter(d_ta, d_tb)), EPSILON);
	}
	
	@Test
	public void testRandomizedStudyRelativeEffect() {
		RandomGenerator rng = mockRandom(0.23);
		ContinuousDataStartingValueGenerator generator = new ContinuousDataStartingValueGenerator(d_network, rng, 2.0);

		Measurement m0 = NetworkModel.findMeasurement(d_s2, d_ta);
		Measurement m1 = NetworkModel.findMeasurement(d_s2, d_tb);
		double s0 = m0.getStdDev() / Math.sqrt(m0.getSampleSize());
		double s1 = m1.getStdDev() / Math.sqrt(m1.getSampleSize());
		assertEquals(m1.getMean() - m0.getMean() + 0.46 * Math.sqrt(s0 * s0 + s1 * s1), generator.getRelativeEffect(d_s2, new BasicParameter(d_ta, d_tb)), EPSILON);
	}
	
	@Test
	public void testGenerateRelativeEffect() {
		ContinuousDataStartingValueGenerator generator = new ContinuousDataStartingValueGenerator(d_network);

		List<EstimateWithPrecision> mds = getMDs(Arrays.asList(d_s2, d_s3), d_ta, d_tb);
		DerSimonianLairdPooling pooling = new DerSimonianLairdPooling(mds);

		assertEquals(pooling.getPooled().getPointEstimate(), 
				generator.getRelativeEffect(new BasicParameter(d_ta, d_tb)), EPSILON);
	}
	
	@Test
	public void testRandomizedRelativeEffect() {
		RandomGenerator rng = mockRandom(0.08);
		ContinuousDataStartingValueGenerator generator = new ContinuousDataStartingValueGenerator(d_network, rng, 3.0);

		List<EstimateWithPrecision> mds = getMDs(Arrays.asList(d_s2, d_s3), d_ta, d_tb);
		DerSimonianLairdPooling pooling = new DerSimonianLairdPooling(mds);

		assertEquals(pooling.getPooled().getPointEstimate() + 3.0 * 0.08 * pooling.getPooled().getStandardError(), 
				generator.getRelativeEffect(new BasicParameter(d_ta, d_tb)), EPSILON);
	}
	
	@Test
	public void testStandardDeviation() {
		ContinuousDataStartingValueGenerator generator = new ContinuousDataStartingValueGenerator(d_network);
		assertEquals(0.5 * d_priorGen.getRandomEffectsSigma(), generator.getStandardDeviation(), EPSILON);
	}
	
	public static EstimateWithPrecision getMD(Study s, Treatment t0, Treatment t1) {
		Measurement m0 = NetworkModel.findMeasurement(s, t0);
		Measurement m1 = NetworkModel.findMeasurement(s, t1);
		return Statistics.meanDifference(m0.getMean(), m0.getStdDev(), m0.getSampleSize(), m1.getMean(), m1.getStdDev(), m1.getSampleSize());
	}
	
	public static List<EstimateWithPrecision> getMDs(List<Study> studies, Treatment t0, Treatment t1) {
		List<EstimateWithPrecision> list = new ArrayList<EstimateWithPrecision>();
		for (Study study : studies) {
			list.add(getMD(study, t0, t1));
		}
		return list;
	}
}