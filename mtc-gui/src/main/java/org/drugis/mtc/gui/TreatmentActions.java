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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.WindowConstants;

import org.drugis.common.beans.ContentAwareListModel;
import org.drugis.common.validation.PropertyUniqueModel;
import org.drugis.common.validation.StringMatchesModel;
import org.drugis.common.validation.StringNotEmptyModel;
import org.drugis.mtc.gui.ListEditor.ListActions;
import org.drugis.mtc.gui.ValidationPanel.Validation;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.NetworkModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.list.ObservableList;
import com.jgoodies.binding.value.ValueModel;

class TreatmentActions implements ListActions<Treatment> {
	private JFrame d_parent;
	private ObservableList<Study> d_studies;

	public TreatmentActions(JFrame parent, ObservableList<Study> studies) {
		d_parent = parent;
		d_studies = studies;
	}

	public String getTypeName() {
		return "treatment";
	}

	public void addAction(ObservableList<Treatment> list) {
		Treatment model = new Treatment();
		showEditDialog(list, model);
		list.add(model);
	}
	public void editAction(ObservableList<Treatment> list, Treatment item) {
		if (item != null) {
			showEditDialog(list, item);
		}
	}
	public void deleteAction(ObservableList<Treatment> list, Treatment item) {
		if (used(item)) {
			JOptionPane.showMessageDialog(d_parent,
				"This treatment is being used by one or more studies. You can't delete it.",
				"Unable to delete", JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (item != null) {
			list.remove(item);
		}
	}

	private boolean used(Treatment item) {
		for (Study s : d_studies) {
			if (NetworkModel.findMeasurement(s, item) != null) {
				return true;
			}
		}
		return false;
	}

	public String getLabel(Treatment item) {
		return item.getId();
	}
	public String getTooltip(Treatment item) {
		String description = item.getDescription();
		if(description == null || description.length() == 0) {
			description = item.getId();
		} 
		return description;
	}

	public ListModel listPresentation(ObservableList<Treatment> list) {
		return new ContentAwareListModel<Treatment>(list, new String[] { Treatment.PROPERTY_ID, Treatment.PROPERTY_DESCRIPTION });
	}

	private void showEditDialog(ObservableList<Treatment> list, Treatment model) {
		final JDialog dialog = new JDialog(d_parent, "Treatment");
		dialog.setLocationByPlatform(true);
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		dialog.setLayout(new BorderLayout());

		JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(new JLabel("ID: "));
		ValueModel idModel = new PropertyAdapter<Treatment>(model, Treatment.PROPERTY_ID, true);
		JTextField field1 = BasicComponentFactory.createTextField(idModel, false);
		field1.setColumns(25);
		panel.add(field1);
		panel.add(new JLabel("Description: "));
		JTextField field2 = BasicComponentFactory.createTextField(new PropertyAdapter<Treatment>(model, Treatment.PROPERTY_DESCRIPTION), false);
		field2.setColumns(25);
		panel.add(field2);
		dialog.add(panel, BorderLayout.CENTER);

		List<Validation> validators = Arrays.asList(
				new Validation(new StringNotEmptyModel(idModel), "The ID may not be empty"),
				new Validation(new PropertyUniqueModel<Treatment>(list, model, Treatment.PROPERTY_ID), "The ID must be unique (there is another treatment with this ID)"),
				new Validation(new StringMatchesModel(idModel, "[A-Za-z0-9_]*"), "The ID may only contain letters, digits and underscores (_)")
				);

		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				dialog.dispose();
			}
		};
		dialog.add(new ValidationPanel(validators, okListener), BorderLayout.SOUTH);

		dialog.pack();
		dialog.setVisible(true);
	}
}
