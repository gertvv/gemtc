package org.drugis.mtc.summary;

import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;

public class RankCounterTest {
	@Test
	public void testSingleRanking() {
		double[] data = new double[] { 1.0, 0.0, 2.0, 0.5, -3.0 };
		int[] rank = new int[] { 4, 2, 5, 3, 1 };
		assertArrayEquals(rank, RankCounter.rank(data));
	}
	
	@Test
	public void testRepeatedRanking() {
		double[][] data = new double[][] { {1.0, 2, 1}, {2.0, 1, 3} };
		int[][] rank = new int[][] { {2, 1}, {1, 2} };
		assertArrayEquals(rank, RankCounter.rank(data));
	}
}
