/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
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
 * ArgumentMaker for individual treatment effects within studies.
 * theta_i,k = mu_i + delta_i,b(i),k
 */
class RealEffectArgumentMaker(
		model: NetworkModel[DichotomousMeasurement, _],
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
			data(sIdx)(baselineIdx) + data(dIdx)(treatmentIdx)
		else
			data(sIdx)(baselineIdx)
	}
}
