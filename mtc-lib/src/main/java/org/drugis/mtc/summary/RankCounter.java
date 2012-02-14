package org.drugis.mtc.summary;

import org.apache.commons.math.stat.ranking.NaturalRanking;
import org.apache.commons.math.stat.ranking.TiesStrategy;

public class RankCounter {
	private static final NaturalRanking s_ranker = new NaturalRanking(TiesStrategy.RANDOM);
	
	/**
	 * Given an n * m array of doubles, give an n * n array of rank-counts.
	 * (The second index representing the ranks).
	 */
	public static int[][] rank(final double[][] data) {
		final int n = data.length;
		final int m = data[0].length;
		final int count[][] = new int[n][n];
		
		for (int i = 0; i < m; ++i) {
			double x[] = new double[n];
			for (int j = 0; j < n; ++j) {
				x[j] = data[j][i];
			}
			int ranks[] = rank(x);
			for (int j = 0; j < n; ++j) {
				count[j][ranks[j] - 1] += 1;
			}
		}
		return count;
	}
	
	/**
	 * Given an n-array of doubles, give an n-array of ranks.
	 */
	public static int[] rank(double[] data) {
		double[] rawRanks = s_ranker.rank(data);
		int[] intRanks = new int[rawRanks.length];
		for (int i = 0; i < rawRanks.length; ++i) {
			intRanks[i] = (int) Math.round(rawRanks[i]);
		}
		return intRanks;
	}
}