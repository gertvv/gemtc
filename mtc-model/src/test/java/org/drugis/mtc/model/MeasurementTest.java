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

import static org.junit.Assert.*;

import org.drugis.mtc.data.DataType;
import org.junit.Test;

public class MeasurementTest {
	@Test
	public void testRestrict() {
		Measurement m = new Measurement();
		m.setMean(1.0);
		m.setStdDev(2.0);
		m.setSampleSize(100);
		m.setResponders(12);
		Treatment t = new Treatment();
		t.setId("A");
		m.setTreatment(t);
		
		Measurement mNone = m.restrict(DataType.NONE);
		assertNull(mNone.getMean());
		assertNull(mNone.getStdDev());
		assertNull(mNone.getSampleSize());
		assertNull(mNone.getResponders());
		assertEquals(m.getTreatment(), mNone.getTreatment());
		
		Measurement mRate = m.restrict(DataType.RATE);
		assertNull(mRate.getMean());
		assertNull(mRate.getStdDev());
		assertEquals(m.getSampleSize(), mRate.getSampleSize());
		assertEquals(m.getResponders(), mRate.getResponders());
		assertEquals(m.getTreatment(), mRate.getTreatment());
		
		Measurement mCont = m.restrict(DataType.CONTINUOUS);
		assertEquals(m.getMean(), mCont.getMean());
		assertEquals(m.getStdDev(), mCont.getStdDev());
		assertEquals(m.getSampleSize(), mCont.getSampleSize());
		assertNull(mCont.getResponders());
		assertEquals(m.getTreatment(), mCont.getTreatment());
	}
}
