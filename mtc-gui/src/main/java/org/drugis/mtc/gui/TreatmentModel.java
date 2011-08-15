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
import org.drugis.common.beans.AbstractObservable;

/**
 * Editable model for Treatment.
 */
public class TreatmentModel extends AbstractObservable {
	public static final String PROPERTY_ID = "id";
	public static final String PROPERTY_DESCRIPTION = "description";

	private String d_id = "";
	private String d_desc = "";

	public void setId(String id) {
		String oldVal = d_id;
		d_id = id;
		firePropertyChange(PROPERTY_ID, oldVal, d_id);
	}

	public String getId() {
		return d_id;
	}

	public void setDescription(String desc) {
		String oldVal = d_desc;
		d_desc = desc;
		firePropertyChange(PROPERTY_DESCRIPTION, oldVal, d_desc);
	}

	public String getDescription() {
		return d_desc;
	}

	public Treatment build() {
		return new Treatment(d_id, d_desc);
	}
}
