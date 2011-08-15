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

import org.drugis.mtc.Treatment;
import org.drugis.mtc.Study;
import org.drugis.mtc.NoneMeasurement;
import org.drugis.mtc.DichotomousMeasurement;
import org.drugis.mtc.ContinuousMeasurement;

import java.util.List;
import java.util.ArrayList;

import org.drugis.common.beans.AbstractObservable;

import com.jgoodies.binding.list.ObservableList;
import com.jgoodies.binding.list.ArrayListModel;

import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;

/**
 * Editable model for Study.
 */
public class StudyModel extends AbstractObservable {
	public static final String PROPERTY_ID = "id";

	private String d_id = "";
	private ObservableList<TreatmentModel> d_treatments = new ArrayListModel<TreatmentModel>();
	private List<Integer> d_sampleSize = new ArrayList<Integer>();
	private List<Integer> d_responders = new ArrayList<Integer>();
	private List<Double> d_mean = new ArrayList<Double>();
	private List<Double> d_stdDev = new ArrayList<Double>();

	public StudyModel() {
		d_treatments.addListDataListener(new ListDataListener() {
			public void contentsChanged(ListDataEvent e) {
				throw new UnsupportedOperationException();
			}
			public void intervalAdded(ListDataEvent e) {
				for (int i = e.getIndex0(); i <= e.getIndex1(); ++i) {
					d_sampleSize.add(i, 0);
					d_responders.add(i, 0);
					d_mean.add(i, 0.0);
					d_stdDev.add(i, 0.0);
				}
			}
			public void intervalRemoved(ListDataEvent e) {
				for (int i = e.getIndex1(); i >= e.getIndex0(); --i) {
					d_sampleSize.remove(i);
					d_responders.remove(i);
					d_mean.remove(i);
					d_stdDev.remove(i);
				}
			}
		});
	}

	public void setId(String id) {
		String oldVal = d_id;
		d_id = id;
		firePropertyChange(PROPERTY_ID, oldVal, d_id);
	}

	public String getId() {
		return d_id;
	}

	public ObservableList<TreatmentModel> getTreatments() {
		return d_treatments;
	}


	public int getResponders(TreatmentModel t) {
		return d_responders.get(d_treatments.indexOf(t));
	}

	public void setResponders(TreatmentModel t, int r) {
		d_responders.set(d_treatments.indexOf(t), r);
	}

	public int getSampleSize(TreatmentModel t) {
		return d_sampleSize.get(d_treatments.indexOf(t));
	}

	public void setSampleSize(TreatmentModel t, int n) {
		d_sampleSize.set(d_treatments.indexOf(t), n);
	}

	public double getMean(TreatmentModel t) {
		return d_mean.get(d_treatments.indexOf(t));
	}

	public void setMean(TreatmentModel t, double m) {
		d_mean.set(d_treatments.indexOf(t), m);
	}

	public double getStdDev(TreatmentModel t) {
		return d_stdDev.get(d_treatments.indexOf(t));
	}

	public void setStdDev(TreatmentModel t, double s) {
		d_stdDev.set(d_treatments.indexOf(t), s);
	}

	public Study<NoneMeasurement> buildNone(List<Treatment> ts) {
		//return new Study<NoneMeasurement>(d_id, new scala.collection.immutable.Set());
		return null;
	}
}

