package org.drugis.mtc

import org.drugis.mtc.util.DerSimonianLairdPooling
import org.drugis.mtc.util.DichotomousDescriptives
import org.drugis.mtc.util.EstimateWithPrecision
import org.drugis.mtc.util.Statistics

import scala.collection.JavaConversions._

class DichotomousDataStartingValueGenerator[
		P <: Parametrization[DichotomousMeasurement]](
val model: NetworkModel[DichotomousMeasurement, P])
extends StartingValueGenerator[DichotomousMeasurement] {
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

	private def includes(p: BasicParameter)(s: Study[M]) = {
		s.treatments.contains(p.base) && s.treatments.contains(p.subject)
	}

	private def basicParameter(p: NetworkModelParameter) = p match {
		case b: BasicParameter => b
		case s: SplitParameter => new BasicParameter(s.base, s.subject)
		case _ => throw new IllegalStateException()
	}
}
