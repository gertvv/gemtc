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
