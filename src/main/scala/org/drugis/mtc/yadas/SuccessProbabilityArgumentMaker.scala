package org.drugis.mtc.yadas

import org.drugis.mtc._
import gov.lanl.yadas.ArgumentMaker

/**
 * ArgumentMaker for individual treatment success probabilities within studies.
 * p_i,k = ilogit(theta_i,k) ; theta_i,k = mu_i + delta_i,b(i),k
 */
class SuccessProbabilityArgumentMaker(model: NetworkModel,
		sIdx: Int, dIdx: Int)
extends ArgumentMaker {
	/**
	 * Calculate "the argument": an array of succes-probabilities, one for
	 * each study-arm.
	 * data[sIdx] should contain study baseline means, one per study
	 * data[dIdx] should contain relative effects, one for each non-baseline
	 */
	def getArgument(data: Array[Array[Double]]): Array[Double] = {
		Array.make(0, 0.0) ++ {
			for {d <- model.data} yield prob(d._1, d._2.treatment, data)
		}
	}

	private def ilogit(x: Double): Double = 1 / (1 + Math.exp(-x))

	private def relativeTreatmentIndex(s: Study[DichotomousMeasurement],
			t: Treatment)
	: Int = {
		model.studyRelativeEffects(s).findIndexOf(x => x._2 == t)
	}

	private def treatmentIndex(s: Study[DichotomousMeasurement],
			t: Treatment)
	: Int = {
		val base = model.relativeEffectIndex(s)
		if (model.studyBaseline(s) == t) -1
		else base + relativeTreatmentIndex(s, t)
	}

	private def prob(s: Study[DichotomousMeasurement],
			t: Treatment, data: Array[Array[Double]])
	: Double = {
		val baselineIdx = model.studyList.indexOf(s)
		val treatmentIdx = treatmentIndex(s, t)

		if (treatmentIdx >= 0) 
			ilogit(data(sIdx)(baselineIdx) + data(dIdx)(treatmentIdx))
		else
			ilogit(data(sIdx)(baselineIdx))
	}
}
