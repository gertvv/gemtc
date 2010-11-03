package org.drugis.mtc.summary;

import java.io.IOException;

import org.drugis.mtc.BasicParameter;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.Treatment;
import org.drugis.mtc.util.FileResults;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import scala.actors.threadpool.Arrays;

public class RankProbabilitySummaryTest {
	private Treatment d_ta;
	private Treatment d_tb;
	private Treatment d_tc;
	private RankProbabilitySummary d_summary;
	private FileResults d_results;
	
	@Before
	public void setUp() throws IOException {
		d_ta = new Treatment("A");
		d_tb = new Treatment("B");
		d_tc = new Treatment("C");
		d_results = new FileResults(
				RankProbabilitySummaryTest.class.getResourceAsStream("rankProbabilitySamples.txt"),
				new Parameter[] { new BasicParameter(d_ta, d_tb), new BasicParameter(d_ta, d_tc) },
				1, 1000);
		d_summary = new RankProbabilitySummary(d_results, Arrays.asList(new Treatment[] { d_ta, d_tb, d_tc }));
	}
	
	@Test
	public void testResults() {
//	              A     B     C    correct rank
//	       [1,] 0.287 0.149 0.564  3
//	       [2,] 0.557 0.206 0.237  2
//	       [3,] 0.156 0.645 0.199  1
//		Generated in R using:
//			> AA <- rep(0, times=1000)
//			> AB <- rnorm(1000, 0.2, 0.3)
//			> AC <- rnorm(1000, -0.1, 0.32)
//			> l <- mapply(function(a, b, c) { c(a, b, c) }, AA, AB, AC)
//			> ranks <- apply(l, 2, rank)
//			> rankprob <- sapply(c(1, 2, 3), function(opt) { sapply(c(1, 2, 3), function(rank) { sum(ranks[opt, ] == rank)/dim(ranks)[2] }) })
		d_results.makeSamplesAvailable();
		assertEquals(0.287, d_summary.getValue(d_ta, 3), 0.001);
		assertEquals(0.557, d_summary.getValue(d_ta, 2), 0.001);
		assertEquals(0.156, d_summary.getValue(d_ta, 1), 0.001);
		assertEquals(0.149, d_summary.getValue(d_tb, 3), 0.001);
		assertEquals(0.206, d_summary.getValue(d_tb, 2), 0.001);
		assertEquals(0.645, d_summary.getValue(d_tb, 1), 0.001);
		assertEquals(0.564, d_summary.getValue(d_tc, 3), 0.001);
		assertEquals(0.237, d_summary.getValue(d_tc, 2), 0.001);
		assertEquals(0.199, d_summary.getValue(d_tc, 1), 0.001);
	}
}
