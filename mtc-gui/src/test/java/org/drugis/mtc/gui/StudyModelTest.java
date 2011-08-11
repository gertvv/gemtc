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

package org.drugis.mtc.gui;

import java.util.Collections;
import java.util.Arrays;

import org.junit.Test;
import static org.junit.Assert.*;
import org.drugis.common.JUnitUtil;
//import static org.easymock.EasyMock.*;

public class StudyModelTest {
	@Test
	public void testInitialValues() {
		StudyModel m = new StudyModel();
		assertEquals("", m.getId());
		assertEquals(Collections.emptyList(), m.getTreatments());
	}

	@Test
	public void testSetters() {
		StudyModel m = new StudyModel();
		JUnitUtil.testSetter(m, TreatmentModel.PROPERTY_ID, "", "Fluox");
		JUnitUtil.testSetter(m, TreatmentModel.PROPERTY_ID, "Fluox", "Parox");
	}

	@Test
	public void testGetTreatments() {
		StudyModel m = new StudyModel();
		TreatmentModel t = new TreatmentModel();
		m.getTreatments().add(t);
		assertEquals(Collections.singletonList(t), m.getTreatments());
	}

	@Test
	public void testSampleSize() {
		StudyModel m = new StudyModel();
		TreatmentModel t1 = new TreatmentModel();
		TreatmentModel t2 = new TreatmentModel();
		TreatmentModel t3 = new TreatmentModel();
		m.getTreatments().add(t1);
		assertEquals(0, m.getSampleSize(t1));
		m.setSampleSize(t1, 5);
		assertEquals(5, m.getSampleSize(t1));
		m.getTreatments().addAll(Arrays.asList(t2, t3));
		assertEquals(0, m.getSampleSize(t2));
		assertEquals(0, m.getSampleSize(t3));
		m.setSampleSize(t2, 8);
		m.setSampleSize(t3, 9);
		assertEquals(8, m.getSampleSize(t2));
		assertEquals(9, m.getSampleSize(t3));

		m.getTreatments().remove(t2);
		assertEquals(5, m.getSampleSize(t1));
		assertEquals(9, m.getSampleSize(t3));
	}

	@Test
	public void testBuild() {
	// FIXME
	}
}
