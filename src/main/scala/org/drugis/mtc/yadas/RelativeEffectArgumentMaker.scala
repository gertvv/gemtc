package org.drugis.mtc.yadas

import org.drugis.mtc._
import gov.lanl.yadas.ArgumentMaker

/**
 * ArgumentMaker for treatment relative effects.
 * delta_ijk ~ N(d_jk, sigma) ; d_jk = f(B + W) ;
 * B basic parameters, W inconsistency factors.
 */
class RelativeEffectArgumentMaker(bIdx: Int, wIdx: Int,
		model: NetworkModel)
extends ArgumentMaker {
	/**
	 * Calculate "the argument": an array of relative effects, one for
	 * each random effect.
	 * data[bIdx] should contain the basic parameters 
	 * data[wIdx] should contain the inconsistency factors
	 */
	def getArgument(data: Array[Array[Double]]): Array[Double] = null
}
