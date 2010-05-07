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
