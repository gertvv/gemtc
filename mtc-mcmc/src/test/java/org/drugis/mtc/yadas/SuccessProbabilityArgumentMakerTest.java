/*
 * This file is part of the GeMTC software for MTC model generation and
 * analysis. GeMTC is distributed from http://drugis.org/gemtc.
 * Copyright (C) 2009-2012 Gert van Valkenhoef.
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