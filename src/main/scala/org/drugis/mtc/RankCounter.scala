package org.drugis.mtc

import org.apache.commons.math.stat.ranking.NaturalRanking
import org.apache.commons.math.stat.ranking.TiesStrategy

object RankCounter {
	val ranker = new NaturalRanking(TiesStrategy.RANDOM)

	/**
	 * Given an nxm array of doubles, give an nxn array of rank-counts (the
	 * first index are treatments)
	 */
	def rank(data: Array[Array[Double]]): Array[Array[Int]] = {
		val n = data.size
		val m = data(0).size
		val count = new Array[Array[Int]](n, n)

		for (i <- 0 until m) {
			val ranks = rank(data.map(a => a(i)))
			for (j <- 0 until n) {
				count(j)(ranks(j) - 1) += 1
			}
		}

		count
	}

	/**
	 * Given an n-array of doubles, give an n-array of ranks.
	 */
	def rank(data: Array[Double]): Array[Int] = {
		ranker.rank(data).map(x => Math.round(x).toInt)
	}
}
