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

package org.drugis.mtc.summary;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

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
