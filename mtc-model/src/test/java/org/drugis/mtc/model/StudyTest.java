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

package org.drugis.mtc.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StudyTest {
	@Test
	public void testContainsTreatment() {
		Study s1 = new Study("1");
		s1.getMeasurements().add(new Measurement(new Treatment("A")));
		s1.getMeasurements().add(new Measurement(new Treatment("B")));
		s1.getMeasurements().add(new Measurement(new Treatment("C")));
		assertTrue(s1.containsTreatment(new Treatment("A")));
		assertTrue(s1.containsTreatment(new Treatment("B")));
		assertTrue(s1.containsTreatment(new Treatment("C")));
		assertFalse(s1.containsTreatment(new Treatment("D")));
	}
}
