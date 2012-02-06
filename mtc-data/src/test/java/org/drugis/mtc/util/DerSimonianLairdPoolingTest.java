/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
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

package org.drugis.mtc.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.drugis.common.stat.EstimateWithPrecision;
import org.drugis.common.stat.Statistics;
import org.junit.Test;

public class DerSimonianLairdPoolingTest {
	public static final double EPSILON = 0.0000001;
	@Test
	public void testOddsRatioPooling() {
		List<EstimateWithPrecision> data = new ArrayList<EstimateWithPrecision>();
		data.add(Statistics.logOddsRatio(63, 144, 73, 142, false));
		data.add(Statistics.logOddsRatio(61, 120, 63, 122, false));
		data.add(Statistics.logOddsRatio(57, 92, 70, 96, false));
		data.add(Statistics.logOddsRatio(84, 119, 85, 117, false));
		data.add(Statistics.logOddsRatio(76, 120, 86, 118, false));

		DerSimonianLairdPooling pooled = new DerSimonianLairdPooling(data);

		assertEquals(2.1408304, pooled.getHeterogeneity(), EPSILON);
		assertEquals(0.2639987, pooled.getPooled().getPointEstimate(), EPSILON);
		assertEquals(0.1215992, pooled.getPooled().getStandardError(), EPSILON);
		assertEquals(0.0, pooled.getHeterogeneityTestStatistic(), EPSILON);
	}
}
