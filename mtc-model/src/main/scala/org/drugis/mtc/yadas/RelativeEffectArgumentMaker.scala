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
import org.apache.commons.math.linear.RealMatrix
import org.apache.commons.math.linear.Array2DRowRealMatrix
import org.apache.commons.math.linear.RealVector
import org.apache.commons.math.linear.ArrayRealVector

/**
 * ArgumentMaker for treatment relative effects.
 * delta_ijk ~ N(d_jk, sigma) ; d_jk = f(B, W) ;
 * B basic parameters, W inconsistency factors.
 */
class RelativeEffectArgumentMaker[M <: Measurement, P <: Parametrization[M]](
	model: NetworkModel[M, P],
	bIdx: Int, wIdx: Option[Int], study: Study[M])
extends ArgumentMaker {
	private val relativeEffects: List[(Treatment, Treatment)] =
		model.studyRelativeEffects(study)

	private def createMatrix(params: List[NetworkModelParameter])
	: RealMatrix = {
		val res = relativeEffects
		val matrix = new Array2DRowRealMatrix(
			res.size, params.size)
		for (i <- 0 until res.size) {
			val re = res(i)
			val reparam = model.parametrization(re._1, re._2)
			for (j <- 0 until params.size) {
				val value = 
					if (reparam.contains(params(j)))
						reparam(params(j)).toDouble
					else 0.0
				matrix.setEntry(i, j, value)
			}
		}
		matrix
	}

	private val basicParamMatrix =
		createMatrix(model.basicParameters)


	private val inconsistencyParamMatrix =
		if (model.inconsistencyParameters.isEmpty) null
		else createMatrix(model.inconsistencyParameters)

	private def calcBasic(data: Array[Array[Double]]): RealVector =
		new ArrayRealVector(basicParamMatrix.operate(data(bIdx)))

	private def calcIncons(data: Array[Array[Double]]): RealVector =
		wIdx match {
			case None => new ArrayRealVector(relativeEffects.size)
			case Some(idx) =>
				if (inconsistencyParamMatrix != null) new ArrayRealVector(
					inconsistencyParamMatrix.operate(data(idx)))
				else new ArrayRealVector(relativeEffects.size)
		}

	/**
	 * Calculate "the argument": an array of relative effects, one for
	 * each random effect.
	 * data[bIdx] should contain the basic parameters 
	 * data[wIdx] should contain the inconsistency factors
	 */
	def getArgument(data: Array[Array[Double]]): Array[Double] = 
		calcBasic(data).add(calcIncons(data)).getData()
}
