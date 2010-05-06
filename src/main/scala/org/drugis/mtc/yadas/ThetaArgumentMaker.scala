package org.drugis.mtc.yadas

import org.drugis.mtc._
import gov.lanl.yadas.ArgumentMaker

/**
 * ArgumentMaker for individual treatment means within studies.
 * theta_i,k = mu_i + delta_i,b(i),k
 */
class ThetaArgumentMaker[M <: Measurement](
		override val model: NetworkModel[M],
		override val sIdx: Int,
		override val dIdx: Int,
		override val study: Study[M])
extends ArgumentMaker with ThetaMaker[M] {
	/**
	 * Calculate "the argument": an array of succes-probabilities, one for
	 * each study-arm.
	 * data[sIdx] should contain study baseline means, one per study
	 * data[dIdx] should contain relative effects, one for each non-baseline
	 */
	def getArgument(data: Array[Array[Double]]): Array[Double] = {
		Array.make(0, 0.0) ++ {
			for {t <- treatments} yield theta(t, data)
		}
	}
}

trait ThetaMaker[M <: Measurement] {
	protected val model: NetworkModel[M]
	protected val sIdx: Int
	protected val dIdx: Int
	protected val study: Study[M]

	protected val treatments = NetworkModel.treatmentList(study.treatments)
	protected val baseline = model.studyBaseline(study)

	private def treatmentIndex(t: Treatment): Int = {
		treatments.indexOf(t)
	}

	private def deltaIndex(t: Treatment): Int = {
		if (t == baseline) -1
		else if (treatmentIndex(baseline) > treatmentIndex(t)) treatmentIndex(t)
		else treatmentIndex(t) - 1
	}

	protected def theta(t: Treatment, data: Array[Array[Double]])
	: Double = {
		val baselineIdx = 0
		val treatmentIdx = deltaIndex(t)

		if (treatmentIdx >= 0) 
			data(sIdx)(baselineIdx) + data(dIdx)(treatmentIdx)
		else
			data(sIdx)(baselineIdx)
	}
}
