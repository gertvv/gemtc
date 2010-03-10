package org.drugis.mtc.yadas

import org.drugis.mtc._
import gov.lanl.yadas.ArgumentMaker
import org.apache.commons.math.linear.RealMatrix
import org.apache.commons.math.linear.Array2DRowRealMatrix
import org.apache.commons.math.linear.RealVector
import org.apache.commons.math.linear.ArrayRealVector

/**
 * ArgumentMaker for treatment relative effects.
 * delta_ijk ~ N(d_jk, sigma) ; d_jk = f(B + W) ;
 * B basic parameters, W inconsistency factors.
 */
class RelativeEffectArgumentMaker(model: NetworkModel,
	bIdx: Int, wIdx: Option[Int])
extends ArgumentMaker {
	private val relativeEffects: List[(Treatment, Treatment)] =
		model.relativeEffects

	private def createMatrix(params: List[NetworkModelParameter])
	: RealMatrix = {
		val res = relativeEffects
		val matrix = new Array2DRowRealMatrix(
			res.size, params.size)
		for (i <- 0 until res.size) {
			val re = res(i)
			val reparam = model.parameterization(re._1, re._2)
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
		createMatrix(model.inconsistencyParameters)

	private def calcBasic(data: Array[Array[Double]]): RealVector =
		new ArrayRealVector(basicParamMatrix.operate(data(bIdx)))

	private def calcIncons(data: Array[Array[Double]]): RealVector =
		wIdx match {
			case None => new ArrayRealVector(relativeEffects.size)
			case Some(idx) => new ArrayRealVector(
				inconsistencyParamMatrix.operate(data(idx)))
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
