package org.drugis.mtc.yadas;

import static org.drugis.common.stat.Statistics.ilogit;
import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.drugis.mtc.model.Treatment;
import org.junit.Test;

import edu.uci.ics.jung.graph.util.Pair;

// Very minimal test because all the important stuff is tested in ThetaArgumentMakerTest.
public class SuccessProbabilityArgumentMakerTest {
	private static final double EPSILON = 0.000000001;

	@Test
	public void testGetArgument() {
		Treatment ta = new Treatment("A");
		Treatment tb = new Treatment("B");
		List<Treatment> treatments = Arrays.asList(ta, tb);
		List<List<Pair<Treatment>>> pmtz = Collections.singletonList(
				Collections.singletonList(new Pair<Treatment>(ta, tb)));
		
		double[] mu = {1.0};
		double[] delta = {-1.0};
		double[][] data = { mu, delta };
		double[] expected = {ilogit(1.0), ilogit(0.0)};
		
		SuccessProbabilityArgumentMaker maker = new SuccessProbabilityArgumentMaker(treatments, pmtz, 0, 1);
		assertArrayEquals(expected, maker.getArgument(data), EPSILON);
	}
}