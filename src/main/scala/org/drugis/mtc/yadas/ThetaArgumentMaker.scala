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
