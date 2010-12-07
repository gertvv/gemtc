package org.drugis.mtc

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Ignore
import org.junit.Before

import org.drugis.mtc.util.Statistics
import org.drugis.mtc.util.EstimateWithPrecision
import org.drugis.mtc.util.DerSimonianLairdPooling
import scala.collection.JavaConversions._

class DichotomousDataStartingValueGeneratorTest extends ShouldMatchersForJUnit {
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

	@Test def testGenerateRandomEffect() {
		val s = network.study("Dierick et al, 1996")
		val lor = getLOR(s, fluox, venla)
		generator.getRandomEffect(s, new BasicParameter(fluox, venla)) should be (
			getLOR(s, fluox, venla).getPointEstimate plusOrMinus EPSILON
		)
	}

	@Test def testGenerateRelativeEffect() {
		val lors: java.util.List[EstimateWithPrecision] = getLORs(List(network.study("Alves et al, 1999"), network.study("Dierick et al, 1996")), fluox, venla)
		val pooling = new DerSimonianLairdPooling(lors)

		generator.getRelativeEffect(new BasicParameter(fluox, venla)) should be (
			pooling.getPooled.getPointEstimate plusOrMinus EPSILON
		)
	}

	@Test def testGenerateVariance() {
		val varParox = (new DerSimonianLairdPooling(getLORs(filterStudies(fluox, parox), fluox, parox))).getPooled.getStandardError
		val varSertr = (new DerSimonianLairdPooling(getLORs(filterStudies(fluox, sertr), fluox, sertr))).getPooled.getStandardError
		val varVenla = (new DerSimonianLairdPooling(getLORs(filterStudies(fluox, venla), fluox, venla))).getPooled.getStandardError

		generator.getRandomEffectsVariance() should be (
			((varParox + varSertr + varVenla) / 3) plusOrMinus EPSILON
		)
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
