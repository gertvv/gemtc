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

import org.drugis.mtc.util.DerSimonianLairdPooling
import org.drugis.mtc.util.DichotomousDescriptives
import org.drugis.mtc.util.EstimateWithPrecision
import org.drugis.mtc.util.Statistics

import scala.collection.JavaConversions._

class DichotomousDataStartingValueGenerator[
		P <: Parametrization[DichotomousMeasurement]](
val model: NetworkModel[DichotomousMeasurement, P])
extends StartingValueGenerator[DichotomousMeasurement]
with StartingValueGeneratorUtil[DichotomousMeasurement] {
	type M = DichotomousMeasurement
	val desc = new DichotomousDescriptives(true)

	override def getBaselineEffect(study: Study[M]) = {
		val m = study.measurements(model.studyBaseline(study))
		desc.logOdds(m.responders, m.sampleSize)
	}

	override def getRandomEffect(study: Study[M], p: BasicParameter) = {
		getLogOddsRatio(study, p).getPointEstimate
	}

	override def getRelativeEffect(p: BasicParameter) = {
		getPooled(p).getPointEstimate
	}

	override def getRandomEffectsVariance() = {
		model.basicParameters.map(p => getPooled(basicParameter(p)).getStandardError).reduceLeft((a, b) => a + b) / model.basicParameters.size
	}

	private def getPooled(p: BasicParameter) = {
		val estimates: java.util.List[EstimateWithPrecision] = getLogOddsRatios(p)
		(new DerSimonianLairdPooling(estimates)).getPooled
	}

	private def getLogOddsRatios(p: BasicParameter)
	: List[EstimateWithPrecision] = {
		model.studyList.filter(includes(p)).map(s => getLogOddsRatio(s, p))
	}

	private def getLogOddsRatio(s: Study[M], p: BasicParameter)
	: EstimateWithPrecision = {
		val m0 = s.measurements(p.base)
		val m1 = s.measurements(p.subject)
		Statistics.logOddsRatio(m0.responders, m0.sampleSize, m1.responders, m1.sampleSize, true)
	}
}

class ContinuousDataStartingValueGenerator[
		P <: Parametrization[ContinuousMeasurement]](
val model: NetworkModel[ContinuousMeasurement, P])
extends StartingValueGenerator[ContinuousMeasurement]
with StartingValueGeneratorUtil[ContinuousMeasurement] {
	type M = ContinuousMeasurement

	override def getBaselineEffect(study: Study[M]) = {
		val m = study.measurements(model.studyBaseline(study))
		m.mean
	}

	override def getRandomEffect(study: Study[M], p: BasicParameter) = {
		getMeanDifference(study, p).getPointEstimate
	}

	override def getRelativeEffect(p: BasicParameter) = {
		getPooled(p).getPointEstimate
	}

	override def getRandomEffectsVariance() = {
		model.basicParameters.map(p => getPooled(basicParameter(p)).getStandardError).reduceLeft((a, b) => a + b) / model.basicParameters.size
	}

	private def getPooled(p: BasicParameter) = {
		val estimates: java.util.List[EstimateWithPrecision] = getMeanDifferences(p)
		(new DerSimonianLairdPooling(estimates)).getPooled
	}

	private def getMeanDifferences(p: BasicParameter)
	: List[EstimateWithPrecision] = {
		model.studyList.filter(includes(p)).map(s => getMeanDifference(s, p))
	}

	private def getMeanDifference(s: Study[M], p: BasicParameter)
	: EstimateWithPrecision = {
		val m0 = s.measurements(p.base)
		val m1 = s.measurements(p.subject)
		Statistics.meanDifference(m0.mean, m0.stdDev, m0.sampleSize, m1.mean, m1.stdDev, m1.sampleSize)
	}
}

trait StartingValueGeneratorUtil[M <: Measurement] {
	def includes(p: BasicParameter)(s: Study[M]) = {
		s.treatments.contains(p.base) && s.treatments.contains(p.subject)
	}

	def basicParameter(p: NetworkModelParameter) = p match {
		case b: BasicParameter => b
		case s: SplitParameter => new BasicParameter(s.base, s.subject)
		case _ => throw new IllegalStateException()
	}
}

object DataStartingValueGenerator {
	def apply[M <: Measurement, P <: Parametrization[M]](
			model: NetworkModel[M, P])
	: StartingValueGenerator[M] = {
		val cls = model.network.measurementType
		if (cls == classOf[DichotomousMeasurement]) {
			new DichotomousDataStartingValueGenerator(model.asInstanceOf[NetworkModel[DichotomousMeasurement, Parametrization[DichotomousMeasurement]]]).asInstanceOf[StartingValueGenerator[M]]
		} else if (cls == classOf[ContinuousMeasurement]) {
			new ContinuousDataStartingValueGenerator(model.asInstanceOf[NetworkModel[ContinuousMeasurement, Parametrization[ContinuousMeasurement]]]).asInstanceOf[StartingValueGenerator[M]]
		} else {
			throw new IllegalStateException("Unknown measurement type " + cls)
		}
	}
}
