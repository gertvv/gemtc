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

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

class RankCounterTest extends ShouldMatchersForJUnit {
	@Test def testSingleRanking() {
		val data = Array(1.0, 0.0, 2.0, 0.5, -3.0)
		val rank = List(4, 2, 5, 3, 1)
		RankCounter.rank(data).toList should be (rank)
	}


	@Test def testRepeatedRanking() {
		val data = Array(
			Array(1.0, 2, 1),
			Array(2.0, 1, 3))
		val rank = List(
			List(2, 1),
			List(1, 2))
	
		RankCounter.rank(data).map(a => a.toList).toList should be (rank)
	}
}
