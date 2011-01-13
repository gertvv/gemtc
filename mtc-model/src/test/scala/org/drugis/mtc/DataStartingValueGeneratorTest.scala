/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2010 Gert van Valkenhoef.
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

package org.drugis.mtc

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Ignore
import org.junit.Before
import org.easymock.EasyMock

import org.drugis.common.stat.Statistics
import org.drugis.common.stat.EstimateWithPrecision
import org.drugis.mtc.util.DerSimonianLairdPooling
import scala.collection.JavaConversions._
import org.apache.commons.math.random.RandomGenerator

class DichotomousDataStartingValueGeneratorTest
extends ShouldMatchersForJUnit {
	val EPSILON = 0.0000001
	val network = Network.dichFromXML(
		<network type="dichotomous">
			<treatments>
				<treatment id="Fluoxetine"></treatment>
				<treatment id="Paroxetine"></treatment>
				<treatment id="Sertraline"></treatment>
				<treatment id="Venlafaxine"></treatment>
			</treatments>
			<studies>
				<study id="Alves et al, 1999">
					<measurement sample="47" treatment="Fluoxetine" responders="4"></measurement>
					<measurement sample="40" treatment="Venlafaxine" responders="1"></measurement>
				</study>
				<study id="Ballus et al, 2000">
					<measurement sample="43" treatment="Paroxetine" responders="3"></measurement>
					<measurement sample="41" treatment="Venlafaxine" responders="1"></measurement>
				</study>
				<study id="Bennie et al, 1995">
					<measurement sample="144" treatment="Fluoxetine" responders="8"></measurement>
					<measurement sample="142" treatment="Sertraline" responders="4"></measurement>
				</study>
				<study id="Dierick et al, 1996">
					<measurement sample="161" treatment="Fluoxetine" responders="7"></measurement>
					<measurement sample="153" treatment="Venlafaxine" responders="9"></measurement>
				</study>
				<study id="Fava et al, 2002">
					<measurement sample="92" treatment="Fluoxetine" responders="11"></measurement>
					<measurement sample="96" treatment="Paroxetine" responders="8"></measurement>
					<measurement sample="96" treatment="Sertraline" responders="6"></measurement>
				</study>
				<study id="Newhouse et al, 2000">
					<measurement sample="119" treatment="Fluoxetine" responders="15"></measurement>
					<measurement sample="117" treatment="Sertraline" responders="17"></measurement>
				</study>
				<study id="Sechter et al, 1999">
					<measurement sample="120" treatment="Fluoxetine" responders="2"></measurement>
					<measurement sample="118" treatment="Sertraline" responders="4"></measurement>
				</study>
			</studies>
		</network>
	)

	val fluox = new Treatment("Fluoxetine")
	val parox = new Treatment("Paroxetine")
	val venla = new Treatment("Venlafaxine")
	val sertr = new Treatment("Sertraline")

	val spanningTree = new Tree[Treatment](
		Set((fluox, parox), (fluox, venla), (fluox, sertr)), fluox)

	val proto = InconsistencyNetworkModel(network, spanningTree)
	val generator = new DichotomousDataStartingValueGenerator(proto)

	@Test def testGenerateBaselineEffect() {
		val m0 = network.study("Alves et al, 1999").measurements(fluox)
		generator.getBaselineEffect(network.study("Alves et al, 1999")) should be (
			Math.log(
				(m0.responders + 0.5) / (m0.sampleSize - m0.responders + 0.5))
			plusOrMinus EPSILON
		)

		val m1 = network.study("Ballus et al, 2000").measurements(parox)
		generator.getBaselineEffect(network.study("Ballus et al, 2000")) should be (
			Math.log(
				(m1.responders + 0.5) / (m1.sampleSize - m1.responders + 0.5))
			plusOrMinus EPSILON
		)
	}

	private def mockRandom(value: Double) = {
		val rng = EasyMock.createMock(classOf[RandomGenerator])
		EasyMock.expect(rng.nextGaussian()).andReturn(value)
		EasyMock.replay(rng)
		rng
	}

	@Test def testRandomizedBaselineEffect1() {
		val m0 = network.study("Alves et al, 1999").measurements(fluox)
		val rng = mockRandom(1.0)
		val rgenerator = new RandomizedDichotomousDataStartingValueGenerator(proto, rng, 1.0)
		rgenerator.getBaselineEffect(network.study("Alves et al, 1999")) should be (
			Math.log(
				(m0.responders + 0.5) / (m0.sampleSize - m0.responders + 0.5)) +
			Math.sqrt(1 / (m0.responders + 0.5) + (1 / (m0.sampleSize - m0.responders + 0.5)))
			plusOrMinus EPSILON
		)
		EasyMock.verify(rng)
	}

	@Test def testRandomizedBaselineEffect2() {
		val rng = mockRandom(0.23)
		val m0 = network.study("Ballus et al, 2000").measurements(parox)

		val rgenerator = new RandomizedDichotomousDataStartingValueGenerator(proto, rng, 2.0)
		rgenerator.getBaselineEffect(network.study("Ballus et al, 2000")) should be (
			Math.log(
				(m0.responders + 0.5) / (m0.sampleSize - m0.responders + 0.5)) +
			0.46 * Math.sqrt(1 / (m0.responders + 0.5) + (1 / (m0.sampleSize - m0.responders + 0.5)))
			plusOrMinus EPSILON
		)
		EasyMock.verify(rng)
	}

	@Test def testGenerateRandomEffect() {
		val s = network.study("Dierick et al, 1996")
		val lor = getLOR(s, fluox, venla)
		generator.getRandomEffect(s, new BasicParameter(fluox, venla)) should be (
			getLOR(s, fluox, venla).getPointEstimate plusOrMinus EPSILON
		)
	}

	@Test def testRandomizedRandomEffect() {
		val rng = mockRandom(-0.34)
		val s = network.study("Dierick et al, 1996")
		val lor = getLOR(s, fluox, venla)
		val rgenerator = new RandomizedDichotomousDataStartingValueGenerator(proto, rng, 2.0)
		rgenerator.getRandomEffect(s, new BasicParameter(fluox, venla)) should be (
			getLOR(s, fluox, venla).getPointEstimate - 2.0 * 0.34 * getLOR(s, fluox, venla).getStandardError
			plusOrMinus EPSILON
		)
		EasyMock.verify(rng)
	}

	@Test def testGenerateRelativeEffect() {
		val lors: java.util.List[EstimateWithPrecision] = getLORs(List(network.study("Alves et al, 1999"), network.study("Dierick et al, 1996")), fluox, venla)
		val pooling = new DerSimonianLairdPooling(lors)

		generator.getRelativeEffect(new BasicParameter(fluox, venla)) should be (
			pooling.getPooled.getPointEstimate plusOrMinus EPSILON
		)
	}

	@Test def testRandomizedRelativeEffect() {
		val lors: java.util.List[EstimateWithPrecision] = getLORs(List(network.study("Alves et al, 1999"), network.study("Dierick et al, 1996")), fluox, venla)
		val pooling = new DerSimonianLairdPooling(lors)

		val rng = mockRandom(0.12)
		val rgenerator = new RandomizedDichotomousDataStartingValueGenerator(proto, rng, 1.5)

		rgenerator.getRelativeEffect(new BasicParameter(fluox, venla)) should be (
			pooling.getPooled.getPointEstimate + 1.5 * 0.12 * pooling.getPooled.getStandardError
			plusOrMinus EPSILON
		)
		EasyMock.verify(rng)
	}

	@Test def testGenerateVariance() {
		val varParox = (new DerSimonianLairdPooling(getLORs(filterStudies(fluox, parox), fluox, parox))).getPooled.getStandardError
		val varSertr = (new DerSimonianLairdPooling(getLORs(filterStudies(fluox, sertr), fluox, sertr))).getPooled.getStandardError
		val varVenla = (new DerSimonianLairdPooling(getLORs(filterStudies(fluox, venla), fluox, venla))).getPooled.getStandardError

		generator.getRandomEffectsVariance() should be (
			((varParox + varSertr + varVenla) / 3) plusOrMinus EPSILON
		)
	}

	@Test def testRandomizedVariance() {
		val rng = EasyMock.createMock(classOf[RandomGenerator])
		EasyMock.expect(rng.nextDouble()).andReturn(0.83)
		EasyMock.replay(rng)

		val rgenerator = new RandomizedDichotomousDataStartingValueGenerator(proto, rng, 1.5)
		rgenerator.getRandomEffectsVariance() should be (
			proto.variancePrior * 0.83 plusOrMinus EPSILON)
		EasyMock.verify(rng)
	}

	def filterStudies(t0: Treatment, t1: Treatment) = {
		proto.studyList.filter(s => s.treatments.contains(t0) && s.treatments.contains(t1))
	}

	def getLORs(studies: List[Study[DichotomousMeasurement]], t0: Treatment, t1: Treatment): List[EstimateWithPrecision] = {
		studies.map(s => getLOR(s, t0, t1))
	}

	def getLOR(s: Study[DichotomousMeasurement], t0: Treatment, t1: Treatment): EstimateWithPrecision = {
		Statistics.logOddsRatio(s.measurements(t0).responders, s.measurements(t0).sampleSize, s.measurements(t1).responders, s.measurements(t1).sampleSize, true)
	}
}

class ContinuousDataStartingValueGeneratorTest
extends ShouldMatchersForJUnit {
	val EPSILON = 0.0000001
	val network = Network.contFromXML(
		<network type="continuous">
			<treatments>
				<treatment id="Fluvoxamine"></treatment>
				<treatment id="Paroxetine"></treatment>
				<treatment id="Sertraline"></treatment>
			</treatments>
			<studies>
				<study id="Aberg-Wistedt et al, 2000">
					<measurement 
					standardDeviation="1.6" mean="-2.5" sample="177" treatment="Paroxetine">
		</measurement>
					<measurement 
					standardDeviation="1.5" mean="-2.6" sample="176" treatment="Sertraline">
		</measurement>
				</study>
				<study id="Kiev and Feiger, 1997">
					<measurement 
					standardDeviation="1.22" mean="-1.93" sample="30" treatment="Fluvoxamine">
		</measurement>
					<measurement 
					standardDeviation="1.18" mean="-1.52" sample="30" treatment="Paroxetine">
		</measurement>
				</study>
				<study id="Fictional">
					<measurement 
					standardDeviation="1.5" mean="-2.01" sample="35" treatment="Fluvoxamine">
		</measurement>
					<measurement 
					standardDeviation="1.45" mean="-1.73" sample="39" treatment="Paroxetine">
		</measurement>
				</study>
				<study id="Nemeroff et al, 1995">
					<measurement 
					standardDeviation="1.23" mean="-1.35" sample="49" treatment="Fluvoxamine">
		</measurement>
					<measurement 
					standardDeviation="0.96" mean="-1.52" sample="46" treatment="Sertraline">
		</measurement>
				</study>
			</studies>
		</network>
	)

	val fluvo = new Treatment("Fluvoxamine")
	val parox = new Treatment("Paroxetine")
	val sertr = new Treatment("Sertraline")

	val spanningTree = new Tree[Treatment](
		Set((parox, fluvo), (parox, sertr)), parox)

	val proto = ConsistencyNetworkModel(network, spanningTree)
	val generator = new ContinuousDataStartingValueGenerator(proto)

	@Test def testGenerateBaselineEffect() {
		val s0 = "Aberg-Wistedt et al, 2000"
		val m0 = network.study(s0).measurements(parox)
		generator.getBaselineEffect(network.study(s0)) should be (
			-2.5 plusOrMinus EPSILON
		)

		val s1 = "Kiev and Feiger, 1997"
		val m1 = network.study(s1).measurements(fluvo)
		generator.getBaselineEffect(network.study(s1)) should be (
			-1.93 plusOrMinus EPSILON
		)
	}

	@Test def testGenerateRandomEffect() {
		val s = network.study("Kiev and Feiger, 1997")
		val m0 = s.measurements(fluvo).mean
		val m1 = s.measurements(parox).mean
		generator.getRandomEffect(s, new BasicParameter(fluvo, parox)) should be (
			(m1 - m0) plusOrMinus EPSILON
		)
	}

	@Test def testGenerateRelativeEffect() {
		val mds: java.util.List[EstimateWithPrecision] = getMDs(List(network.study("Kiev and Feiger, 1997"), network.study("Fictional")), fluvo, parox)
		val pooling = new DerSimonianLairdPooling(mds)

		generator.getRelativeEffect(new BasicParameter(fluvo, parox)) should be (
			pooling.getPooled.getPointEstimate plusOrMinus EPSILON
		)
	}

	@Test def testGenerateVariance() {
		val varFluvo = (new DerSimonianLairdPooling(getMDs(filterStudies(parox, fluvo), parox, fluvo))).getPooled.getStandardError
		val varSertr = (new DerSimonianLairdPooling(getMDs(filterStudies(parox, sertr), parox, sertr))).getPooled.getStandardError

		generator.getRandomEffectsVariance() should be (
			((varFluvo + varSertr) / 2) plusOrMinus EPSILON
		)
	}

	def getMDs(studies: List[Study[ContinuousMeasurement]], t0: Treatment, t1: Treatment) = {
		studies.map(s => getMD(s, t0, t1))
	}

	def getMD(s: Study[ContinuousMeasurement],  t0: Treatment, t1: Treatment) = {
		val m0 = s.measurements(t0)
		val m1 = s.measurements(t1)
		Statistics.meanDifference(m0.mean, m0.stdDev, m0.sampleSize, m1.mean, m1.stdDev, m1.sampleSize)
	}

	def filterStudies(t0: Treatment, t1: Treatment) = {
		proto.studyList.filter(s => s.treatments.contains(t0) && s.treatments.contains(t1))
	}
}
