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
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.drugis.common.stat.EstimateWithPrecision;
import org.drugis.common.stat.Statistics;
import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.util.DerSimonianLairdPooling;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class DichotomousDataStartingValueTest {
	private static final double EPSILON = 0.0000001;
	private Treatment d_ta;
	private Treatment d_tb;
	private Treatment d_tc;
	private Treatment d_td;
	private Study d_s1;
	private Study d_s2;
	private Study d_s3;
	private Study d_s4;
	private Study d_s5;
	private Study d_s6;
	private Study d_s7;
	private Network d_network;
	private PriorGenerator d_priorGen;
	
	@Before
	public void setUp() {
		d_ta = new Treatment("A");
		d_tb = new Treatment("B");
		d_tc = new Treatment("C");
		d_td = new Treatment("D");
		d_s1 = new Study("1");
		d_s1.getMeasurements().add(new Measurement(d_ta, 4, 47));
		d_s1.getMeasurements().add(new Measurement(d_td, 1, 40));
		d_s2 = new Study("2");
		d_s2.getMeasurements().add(new Measurement(d_tb, 3, 43));
		d_s2.getMeasurements().add(new Measurement(d_td, 1, 41));
		d_s3 = new Study("3");
		d_s3.getMeasurements().add(new Measurement(d_ta, 7, 161));
		d_s3.getMeasurements().add(new Measurement(d_tc, 9, 153));
		d_s4 = new Study("4");
		d_s4.getMeasurements().add(new Measurement(d_ta, 7, 161));
		d_s4.getMeasurements().add(new Measurement(d_td, 9, 153));
		d_s5 = new Study("5");
		d_s5.getMeasurements().add(new Measurement(d_ta, 11, 92));
		d_s5.getMeasurements().add(new Measurement(d_tb, 8, 96));
		d_s5.getMeasurements().add(new Measurement(d_tc, 6, 96));
		d_s6 = new Study("6");
		d_s6.getMeasurements().add(new Measurement(d_ta, 15, 119));
		d_s6.getMeasurements().add(new Measurement(d_tc, 17, 117));
		d_s7 = new Study("7");
		d_s7.getMeasurements().add(new Measurement(d_ta, 2, 120));
		d_s7.getMeasurements().add(new Measurement(d_tc, 4, 118));
		
		d_network = new Network();
		d_network.getTreatments().addAll(Arrays.asList(d_ta, d_tb, d_tc, d_td));
		d_network.getStudies().addAll(Arrays.asList(d_s1, d_s2, d_s3, d_s4, d_s5, d_s6, d_s7));
		
		d_priorGen = new PriorGenerator(d_network);
	}
	
	@Test
	public void testGenerateTreatmentEffect() {
		StartingValueGenerator generator = new DichotomousDataStartingValueGenerator(d_network);

		Measurement m0 = d_s1.getMeasurements().get(0);
		assertEquals(d_ta, m0.getTreatment()); // Check assumption
		assertEquals(Math.log((m0.getResponders() + 0.5) / (m0.getSampleSize() - m0.getResponders() + 0.5)),
				generator.getTreatmentEffect(d_s1, d_ta), EPSILON);

		Measurement m1 = d_s2.getMeasurements().get(0);
		assertEquals(d_tb, m1.getTreatment()); // Check assumption
		assertEquals(Math.log((m1.getResponders() + 0.5) / (m1.getSampleSize() - m1.getResponders() + 0.5)),
				generator.getTreatmentEffect(d_s2, d_tb), EPSILON);
	}
	
	private RandomGenerator mockRandom(double value) {
		RandomGenerator rng = EasyMock.createMock(RandomGenerator.class);
		EasyMock.expect(rng.nextGaussian()).andReturn(value);
		EasyMock.replay(rng);
		return rng;
	}

	@Test 
	public void testRandomizedTreatmentEffect1() {
		RandomGenerator rng = mockRandom(1.0);
		StartingValueGenerator generator = new DichotomousDataStartingValueGenerator(d_network, rng, 1.0);

		Measurement m0 = d_s1.getMeasurements().get(0);

		assertEquals(Math.log((m0.getResponders() + 0.5) / (m0.getSampleSize() - m0.getResponders() + 0.5)) +
				Math.sqrt(1 / (m0.getResponders() + 0.5) + (1 / (m0.getSampleSize() - m0.getResponders() + 0.5))),
				generator.getTreatmentEffect(d_s1, d_ta), EPSILON);
		EasyMock.verify(rng);
	}
	
	@Test 
	public void testRandomizedTreatmentEffect2() {
		RandomGenerator rng = mockRandom(0.23);
		StartingValueGenerator generator = new DichotomousDataStartingValueGenerator(d_network, rng, 2.0);

		Measurement m0 = d_s2.getMeasurements().get(0);

		assertEquals(Math.log((m0.getResponders() + 0.5) / (m0.getSampleSize() - m0.getResponders() + 0.5)) +
				0.46 * Math.sqrt(1 / (m0.getResponders() + 0.5) + (1 / (m0.getSampleSize() - m0.getResponders() + 0.5))),
				generator.getTreatmentEffect(d_s2, d_tb), EPSILON);
		EasyMock.verify(rng);
	}
	
	@Test
	public void testGenerateStudyRelativeEffect() {
		StartingValueGenerator generator = new DichotomousDataStartingValueGenerator(d_network);

		assertEquals(getLOR(d_s4, d_ta, d_td).getPointEstimate(),
				generator.getRelativeEffect(d_s4, new BasicParameter(d_ta, d_td)), EPSILON);
	}
	
	@Test
	public void testRandomizedStudyRelativeEffect() {
		RandomGenerator rng = mockRandom(-0.34);
		StartingValueGenerator generator = new DichotomousDataStartingValueGenerator(d_network, rng, 2.0);

		EstimateWithPrecision lor = getLOR(d_s4, d_ta, d_td);
		assertEquals(lor.getPointEstimate() - 2.0 * 0.34 * lor.getStandardError(),
				generator.getRelativeEffect(d_s4, new BasicParameter(d_ta, d_td)), EPSILON);
		EasyMock.verify(rng);
	}

	@Test
	public void testGenerateRelativeEffect() {
		StartingValueGenerator generator = new DichotomousDataStartingValueGenerator(d_network);

		List<EstimateWithPrecision> lors = getLORs(Arrays.asList(d_s1, d_s4), d_ta, d_td);
		DerSimonianLairdPooling pooling = new DerSimonianLairdPooling(lors);

		assertEquals(pooling.getPooled().getPointEstimate(),
				generator.getRelativeEffect(new BasicParameter(d_ta, d_td)), EPSILON);
	}
	
	@Test
	public void testRandomizedRelativeEffect() {
		RandomGenerator rng = mockRandom(0.12);
		StartingValueGenerator generator = new DichotomousDataStartingValueGenerator(d_network, rng, 1.5);

		List<EstimateWithPrecision> lors = getLORs(Arrays.asList(d_s1, d_s4), d_ta, d_td);
		DerSimonianLairdPooling pooling = new DerSimonianLairdPooling(lors);

		assertEquals(pooling.getPooled().getPointEstimate() + 1.5 * 0.12 * pooling.getPooled().getStandardError(),
				generator.getRelativeEffect(new BasicParameter(d_ta, d_td)), EPSILON);
		EasyMock.verify(rng);
	}

	@Test
	public void testStandardDeviation() {
		StartingValueGenerator generator = new DichotomousDataStartingValueGenerator(d_network);
		assertEquals(0.5 * d_priorGen.getRandomEffectsSigma(), generator.getStandardDeviation(), EPSILON);
	}
	
	@Test
	public void testRandomizedStandardDeviation() {
		RandomGenerator rng = EasyMock.createMock(RandomGenerator.class);
		double random = 0.156666;
		EasyMock.expect(rng.nextDouble()).andReturn(random);
		EasyMock.replay(rng);
		
		StartingValueGenerator generator = new DichotomousDataStartingValueGenerator(d_network, rng, 1.5);
		assertEquals(random * d_priorGen.getRandomEffectsSigma(), generator.getStandardDeviation(), EPSILON);
	}
	
	public static EstimateWithPrecision getLOR(Study s, Treatment t0, Treatment t1) {
		Measurement m0 = NetworkModel.findMeasurement(s, t0);
		Measurement m1 = NetworkModel.findMeasurement(s, t1);
		return Statistics.logOddsRatio(m0.getResponders(), m0.getSampleSize(), m1.getResponders(), m1.getSampleSize(), true);
	}
	
	public static List<EstimateWithPrecision> getLORs(Collection<Study> studies, Treatment t0, Treatment t1) {
		List<EstimateWithPrecision> list = new ArrayList<EstimateWithPrecision>();
		for (Study study : studies) {
			list.add(getLOR(study, t0, t1));
		}
		return list;
	}
}
