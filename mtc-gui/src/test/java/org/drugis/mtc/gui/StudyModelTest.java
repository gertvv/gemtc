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
	public void testBuild() {
	// FIXME
	}
}
