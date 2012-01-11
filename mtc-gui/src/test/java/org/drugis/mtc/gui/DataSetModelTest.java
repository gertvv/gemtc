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

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.drugis.common.JUnitUtil;
import org.drugis.mtc.ContinuousMeasurement;
import org.drugis.mtc.DichotomousMeasurement;
import org.drugis.mtc.Network;
import org.drugis.mtc.NoneMeasurement;
import org.drugis.mtc.Study;
import org.drugis.mtc.Treatment;
import org.drugis.mtc.util.ScalaUtil;
import org.junit.Test;

import scala.xml.Elem;

public class DataSetModelTest {
	@SuppressWarnings("unchecked")
	@Test
	public void testBuildNone() {
		DataSetModel model = new DataSetModel();
		model.getDescription().setValue("test description");
		TreatmentModel ta = new TreatmentModel();
		ta.setId("A");
		TreatmentModel tb = new TreatmentModel();
		tb.setId("B");
		model.getTreatments().add(ta);
		model.getTreatments().add(tb);
		StudyModel study = new StudyModel();
		study.setId("01");
		study.getTreatments().addAll(model.getTreatments());
		model.getStudies().add(study);
		model.getMeasurementType().setValue(MeasurementType.NONE);
		
		Set<Treatment> ts = new HashSet<Treatment>(Arrays.asList(new Treatment("A"), new Treatment("B")));
		Map<Treatment, NoneMeasurement> map = new HashMap<Treatment, NoneMeasurement>();
		map.put(new Treatment("A"), new NoneMeasurement(new Treatment("A")));
		map.put(new Treatment("B"), new NoneMeasurement(new Treatment("B")));
		Study<NoneMeasurement> s01 = new Study<NoneMeasurement>("01", ScalaUtil.toScalaMap(map));
		Set<Study<NoneMeasurement>> ss = new HashSet<Study<NoneMeasurement>>(Arrays.asList(s01));
		Network<NoneMeasurement> expected = new Network<NoneMeasurement>("test description", ScalaUtil.toScalaSet(ts), ScalaUtil.toScalaSet(ss));

		Elem expectedXML = expected.toXML();
		assertEquals(expectedXML.toString(), model.build().toXML().toString());
		assertEquals(expectedXML.toString(), DataSetModel.build(Network.fromXML(expectedXML)).build().toXML().toString()); // round-trip
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testBuildContinuous() {
		DataSetModel model = new DataSetModel();
		TreatmentModel ta = new TreatmentModel();
		ta.setId("A");
		TreatmentModel tb = new TreatmentModel();
		tb.setId("B");
		model.getTreatments().add(ta);
		model.getTreatments().add(tb);
		StudyModel study = new StudyModel();
		study.setId("01");
		study.getTreatments().addAll(model.getTreatments());
		
		study.setMean(ta, 0.5);
		study.setStdDev(ta, 0.3);
		study.setSampleSize(ta, 30);
		
		study.setMean(tb, 1.0);
		study.setStdDev(tb, 0.4);
		study.setSampleSize(tb, 30);
		
		model.getStudies().add(study);
		model.getMeasurementType().setValue(MeasurementType.CONTINUOUS);
		
		Set<Treatment> ts = new HashSet<Treatment>(Arrays.asList(new Treatment("A"), new Treatment("B")));
		Map<Treatment, ContinuousMeasurement> map = new HashMap<Treatment, ContinuousMeasurement>();
		map.put(new Treatment("A"), new ContinuousMeasurement(new Treatment("A"), 0.5, 0.3, 30));
		map.put(new Treatment("B"), new ContinuousMeasurement(new Treatment("B"), 1.0, 0.4, 30));
		Study<ContinuousMeasurement> s01 = new Study<ContinuousMeasurement>("01", ScalaUtil.toScalaMap(map));
		Set<Study<ContinuousMeasurement>> ss = new HashSet<Study<ContinuousMeasurement>>(Arrays.asList(s01));
		Network<ContinuousMeasurement> expected = new Network<ContinuousMeasurement>(ScalaUtil.toScalaSet(ts), ScalaUtil.toScalaSet(ss));
		
		Elem expectedXML = expected.toXML();
		assertEquals(expectedXML, model.build().toXML());
		assertEquals(expectedXML, DataSetModel.build(Network.fromXML(expectedXML)).build().toXML()); // round-trip
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testBuildDichotomous() {
		DataSetModel model = new DataSetModel();
		TreatmentModel ta = new TreatmentModel();
		ta.setId("A");
		TreatmentModel tb = new TreatmentModel();
		tb.setId("B");
		model.getTreatments().add(ta);
		model.getTreatments().add(tb);
		StudyModel study = new StudyModel();
		study.setId("01");
		study.getTreatments().addAll(model.getTreatments());
		
		study.setResponders(ta, 15);
		study.setSampleSize(ta, 30);
		
		study.setResponders(tb, 16);
		study.setSampleSize(tb, 30);
		
		model.getStudies().add(study);
		model.getMeasurementType().setValue(MeasurementType.DICHOTOMOUS);
		
		Set<Treatment> ts = new HashSet<Treatment>(Arrays.asList(new Treatment("A"), new Treatment("B")));
		Map<Treatment, DichotomousMeasurement> map = new HashMap<Treatment, DichotomousMeasurement>();
		map.put(new Treatment("A"), new DichotomousMeasurement(new Treatment("A"), 15, 30));
		map.put(new Treatment("B"), new DichotomousMeasurement(new Treatment("B"), 16, 30));
		Study<DichotomousMeasurement> s01 = new Study<DichotomousMeasurement>("01", ScalaUtil.toScalaMap(map));
		Set<Study<DichotomousMeasurement>> ss = new HashSet<Study<DichotomousMeasurement>>(Arrays.asList(s01));
		Network<DichotomousMeasurement> expected = new Network<DichotomousMeasurement>(ScalaUtil.toScalaSet(ts), ScalaUtil.toScalaSet(ss));
		
		Elem expectedXML = expected.toXML();
		assertEquals(expectedXML, model.build().toXML());
		assertEquals(expectedXML, DataSetModel.build(Network.fromXML(expectedXML)).build().toXML()); // round-trip
	}
	
	@Test
	public void testFile() {
		JUnitUtil.testSetter(new DataSetModel(), DataSetModel.PROPERTY_FILE, null, new File("/"));
	}
}
