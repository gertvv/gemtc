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
 * ArgumentMaker for (a row of the) variance-covariance matrix of RE Gaussian.
 */
class SigmaRowArgumentMaker[M <: Measurement](study: Study[M],
		sigmaIdx: Int, rowIdx: Int)
extends ArgumentMaker {
	/**
	 * Calculate "the argument": a row of the var/covar matrix.
	 * data[sigmaIdx][0] should contain sqrt(sigma_0)
	 * @return row rowIdx of the var/covar matrix.
	 */
	def getArgument(data: Array[Array[Double]]): Array[Double] = {
		val sd = data(sigmaIdx)(0)
		val vari = sd * sd
		val cova = vari / 2
		val arr = Array.make(study.treatments.size - 1, cova)
		arr(rowIdx) = vari
		arr
	}
}

object SigmaMatrixArgumentMaker {
	def apply[M <: Measurement](study: Study[M], sigmaIdx: Int)
	: List[ArgumentMaker] = {
		(0 until (study.treatments.size - 1)).map(
			rowIdx => new SigmaRowArgumentMaker(study, sigmaIdx, rowIdx)
		).toList
	}
}	
