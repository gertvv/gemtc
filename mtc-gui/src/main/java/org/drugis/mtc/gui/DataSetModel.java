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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.xml.bind.JAXBException;

import org.drugis.common.beans.AbstractObservable;
import org.drugis.common.beans.ContentAwareListModel;
import org.drugis.common.event.IndifferentListDataListener;
import org.drugis.mtc.data.DataType;
import org.drugis.mtc.model.JAXBHandler;
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
	public static final String PROPERTY_REVISION = "revision";
	private final Network d_network;
	private File d_file = null;
	private TableModel d_measurementTableModel;
	private long d_rev = 0;

	public static Network defaultNetwork() {
		Network network = new Network();
		network.setDescription("???");
		network.setType(DataType.RATE);
		return network;
	}

	/**
	 * Create an empty data set.
	 */
	public DataSetModel() {
		this(defaultNetwork());
	}

	/**
	 * Create a data set based on the given network.
	 */
	public DataSetModel(Network network) {
		d_network = network;
		d_measurementTableModel = new MeasurementTableModel(this);
		d_measurementTableModel.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				firePropertyChange(PROPERTY_REVISION, d_rev, ++d_rev);
			}
		});
		getDescription().addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				firePropertyChange(PROPERTY_REVISION, d_rev, ++d_rev);
			}
		});
		ContentAwareListModel<Treatment> treatments = new ContentAwareListModel<Treatment>(getTreatments());
		treatments.addListDataListener(new IndifferentListDataListener() {
			protected void update() {
				firePropertyChange(PROPERTY_REVISION, d_rev, ++d_rev);
			}
		});
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

	public TableModel getMeasurementTableModel() {
		return d_measurementTableModel;
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

	public Network cloneNetwork() throws JAXBException, IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JAXBHandler.writeNetwork(getNetwork(), out);
		out.close();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(out.toByteArray());
		Network clone = JAXBHandler.readNetwork(inputStream);
		inputStream.close();
		return clone;
	}

	public long getRevision() {
		return d_rev;
	}
}
