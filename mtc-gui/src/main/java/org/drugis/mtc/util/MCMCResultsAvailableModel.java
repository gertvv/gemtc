/*
 * This file is part of ADDIS (Aggregate Data Drug Information System).
 * ADDIS is distributed from http://drugis.org/.
 * Copyright (C) 2009 Gert van Valkenhoef, Tommi Tervonen.
 * Copyright (C) 2010 Gert van Valkenhoef, Tommi Tervonen, 
 * Tijs Zwinkels, Maarten Jacobs, Hanno Koeslag, Florin Schimbinschi, 
 * Ahmad Kamal, Daniel Reid.
 * Copyright (C) 2011 Gert van Valkenhoef, Ahmad Kamal, 
 * Daniel Reid, Florin Schimbinschi.
 * Copyright (C) 2012 Gert van Valkenhoef, Daniel Reid, 
 * Joël Kuiper, Wouter Reckman.
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

package org.drugis.mtc.util;

import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;

import com.jgoodies.binding.value.AbstractValueModel;

@SuppressWarnings("serial")
public class MCMCResultsAvailableModel extends AbstractValueModel implements MCMCResultsListener {

	private boolean d_val;

	public MCMCResultsAvailableModel(MCMCResults results) {
		d_val = results.getNumberOfSamples() > 0;
		results.addResultsListener(this);
	}

	public Boolean getValue() {
		return d_val;
	}

	public void setValue(Object newValue) {
		throw new IllegalAccessError("MCMCResultsAvailableModel is read-only");
	}

	public void resultsEvent(MCMCResultsEvent event) {
		boolean oldval = d_val;
		d_val = event.getSource().getNumberOfSamples() > 0;
		fireValueChange(oldval, d_val);
	}
}
