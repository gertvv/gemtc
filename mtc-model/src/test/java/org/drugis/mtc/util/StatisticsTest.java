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

package org.drugis.mtc.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class StatisticsTest {
	private static final double EPSILON = 0.000001;
	@Test
	public void testMeanDifference() {
		EstimateWithPrecision e = Statistics.meanDifference(
			-2.5, 1.6, 177, -2.6, 1.5, 176);
		assertEquals(-0.1, e.getPointEstimate(), EPSILON);
		assertEquals(0.1650678, e.getStandardError(), EPSILON);
	}
}
