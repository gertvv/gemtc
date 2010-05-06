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
