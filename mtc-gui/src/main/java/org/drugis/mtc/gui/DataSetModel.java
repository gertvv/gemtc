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

import java.io.File;

import org.drugis.common.beans.AbstractObservable;
import org.drugis.mtc.data.DataType;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;

import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.list.ObservableList;
import com.jgoodies.binding.value.ValueModel;

/**
 * Editable model for an MTC data set.
 */
public class DataSetModel extends AbstractObservable {
	public static final String PROPERTY_FILE = "file";
	private final Network d_network;
	private File d_file = null;
	
	/**
	 * Create an empty data set.
	 */
	public DataSetModel() {
		d_network = new Network();
		d_network.setDescription("???");
		d_network.setType(DataType.RATE);
	}
	
	/**
	 * Create a data set based on the given network.
	 */
	public DataSetModel(Network network) {
		d_network = network;
	}
	
	public ValueModel getDescription() {
		return new PropertyAdapter<Network>(d_network, Network.PROPERTY_DESCRIPTION, true);
	}

	public ValueModel getMeasurementType() {
		return new PropertyAdapter<Network>(d_network, Network.PROPERTY_TYPE, true);
	}
	
	public ObservableList<Treatment> getTreatments() {
		return d_network.getTreatments();
	}
	
	public ObservableList<Study> getStudies() {
		return d_network.getStudies();
	}
	
	public void setFile(File file) {
		File oldValue = d_file;
		d_file = file;
		firePropertyChange(PROPERTY_FILE, oldValue, d_file);
	}

	public File getFile() {
		return d_file;
	}

	public Network getNetwork() {
		return d_network;
	}
}
