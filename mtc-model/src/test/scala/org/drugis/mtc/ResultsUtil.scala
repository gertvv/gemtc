package org.drugis.mtc

object ResultsUtil {
	def getSamples(r: MCMCResults, p: Int, c: Int): Array[Double] = {
		(0 until r.getNumberOfSamples).map(
			i => r.getSample(p, c, i)).toArray
	}
	def getSamples(r: MCMCResults, p: Parameter, c: Int): Array[Double] = {
		getSamples(r, r.findParameter(p), c)
	}
}
