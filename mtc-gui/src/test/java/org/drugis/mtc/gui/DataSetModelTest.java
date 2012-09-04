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

package org.drugis.mtc.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import org.drugis.common.JUnitUtil;
import org.drugis.mtc.data.DataType;
import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.easymock.EasyMock;
import org.junit.Test;

public class DataSetModelTest {
	@Test
	public void testFile() {
		JUnitUtil.testSetter(new DataSetModel(), DataSetModel.PROPERTY_FILE, null, new File("/"));
	}

	@Test
	public void testRevision() {
		final DataSetModel model = new DataSetModel();

		assertRevisions(model, 0L, 1L, new Runnable() {
			public void run() { model.getNetwork().setType(DataType.CONTINUOUS); }
		});
		assertRevisions(model, 1L, 2L, new Runnable() {
			public void run() { model.getNetwork().setDescription("Ace of Spades"); }
		});

		final Treatment t1 = new Treatment();
		final Treatment t2 = new Treatment("X2", "Ble");
		assertRevisions(model, 2L, 5L, new Runnable() {
			public void run() {
				model.getTreatments().add(t1);
				t1.setId("X1");
				t1.setDescription("Bla");
			}
		});
		model.getTreatments().add(t2);

		final Study s = new Study();
		assertRevisions(model, 6L, 10L, new Runnable() {
			public void run() {
				model.getStudies().add(s);
				s.setId("AAA");
				s.getMeasurements().add(new Measurement(t1));
				s.getMeasurements().add(new Measurement(t2));
			}
		});

		assertRevisions(model, 10L, 12L, new Runnable() {
			public void run() {
				model.getMeasurementTableModel().setValueAt(1.5, 1, 1); // mean
				model.getMeasurementTableModel().setValueAt(105, 1, 3); // sample size
			}
		});
	}

	private static void assertRevisions(final DataSetModel model, long r0, long r1, Runnable runnable) {
		PropertyChangeListener listener = EasyMock.createStrictMock(PropertyChangeListener.class);
		for (long r = r0; r < r1; ++r) {
			listener.propertyChange(JUnitUtil.eqPropertyChangeEvent(new PropertyChangeEvent(model, DataSetModel.PROPERTY_REVISION, r, r + 1)));
		}
		EasyMock.replay(listener);
		model.addPropertyChangeListener(listener);
		runnable.run();
		EasyMock.verify(listener);
		model.removePropertyChangeListener(listener);
	}
}
