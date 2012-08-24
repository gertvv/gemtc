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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;

import com.jgoodies.binding.value.AbstractValueModel;

@SuppressWarnings("serial")
public class MCMCResultsMemoryUsageModel extends AbstractValueModel implements MCMCResultsListener {

	private MCMCResults d_results;

	public MCMCResultsMemoryUsageModel(MCMCResults results) {
		d_results = results;
		d_results.addResultsListener(this);
	}

	public String getValue() {
		NumberFormat format = new DecimalFormat("0.0");

		if(d_results.getNumberOfSamples() > 0) {
			if(getKiloByteSize() < 100) {
				return format.format(getKiloByteSize()) + " KB";
			}
			return format.format(getKiloByteSize() / 1000.0) + " MB";
		}
		return "0.0 KB";
	}

	private double getKiloByteSize() {
		return getByteSize() / 1000.0;
	}
	
	private long getByteSize() {
		return (long) d_results.getNumberOfSamples() * d_results.getNumberOfChains() * d_results.getParameters().length * Double.SIZE / 8;
	}

	public void setValue(Object newValue) {
		throw new IllegalAccessError("MCMCResultsMemoryUsageModel is read-only");
	}

	public void resultsEvent(MCMCResultsEvent event) {
		fireValueChange(null, getValue());
	}
	
//	public void clear() {
//		d_results.clear();
//		fireValueChange(null, getValue());
//	}
}
