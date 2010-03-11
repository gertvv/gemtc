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
