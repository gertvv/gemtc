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

//import org.drugis.mtc.Treatment;
//import org.drugis.mtc.Study;
import org.drugis.common.beans.AbstractObservable;

import com.jgoodies.binding.list.ObservableList;
import com.jgoodies.binding.list.ArrayListModel;

/**
 * Editable model for Study.
 */
public class StudyModel extends AbstractObservable {
	public static final String PROPERTY_ID = "id";

	private String d_id = "";
	private ObservableList<TreatmentModel> d_treatments = new ArrayListModel<TreatmentModel>();

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

	//public Study build(MeasurementsModel ...) {
	//	return new Treatment(d_id, d_desc);
	//}
}

