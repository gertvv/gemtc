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

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.list.ObservableList;
import com.jgoodies.binding.value.ValueModel;

class TreatmentActions implements ListActions<TreatmentModel> {
	private JFrame d_parent;
	private ObservableList<StudyModel> d_studies;

	public TreatmentActions(JFrame parent, ObservableList<StudyModel> studies) {
		d_parent = parent;
		d_studies = studies;
	}

	public String getTypeName() {
		return "treatment";
	}

	public void addAction(ObservableList<TreatmentModel> list) {
		TreatmentModel model = new TreatmentModel();
		showEditDialog(list, model);
		list.add(model);
	}
	public void editAction(ObservableList<TreatmentModel> list, TreatmentModel item) {
		if (item != null) {
			showEditDialog(list, item);
		}
	}
	public void deleteAction(ObservableList<TreatmentModel> list, TreatmentModel item) {
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

	private boolean used(TreatmentModel item) {
		for (StudyModel s : d_studies) {
			if (s.getTreatments().contains(item)) {
				return true;
			}
		}
		return false;
	}

	public String getLabel(TreatmentModel item) {
		return item.getId();
	}
	public String getTooltip(TreatmentModel item) {
		return item.getDescription();
	}

	public ListModel listPresentation(ObservableList<TreatmentModel> list) {
		return new ContentAwareListModel<TreatmentModel>(list, new String[] { TreatmentModel.PROPERTY_ID, TreatmentModel.PROPERTY_DESCRIPTION });
	}

	private void showEditDialog(ObservableList<TreatmentModel> list, TreatmentModel model) {
		final JDialog dialog = new JDialog(d_parent, "Treatment");
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		dialog.setLayout(new BorderLayout());

		JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(new JLabel("ID: "));
		ValueModel idModel = new PropertyAdapter<TreatmentModel>(model, TreatmentModel.PROPERTY_ID, true);
		JTextField field1 = BasicComponentFactory.createTextField(idModel, false);
		field1.setColumns(25);
		panel.add(field1);
		panel.add(new JLabel("Description: "));
		JTextField field2 = BasicComponentFactory.createTextField(new PropertyAdapter<TreatmentModel>(model, TreatmentModel.PROPERTY_DESCRIPTION), false);
		field2.setColumns(25);
		panel.add(field2);
		dialog.add(panel, BorderLayout.CENTER);

		List<Validation> validators = Arrays.asList(
				new Validation(new StringNotEmptyModel(idModel), "The ID may not be empty"),
				new Validation(new PropertyUniqueModel<TreatmentModel>(list, model, TreatmentModel.PROPERTY_ID), "The ID must be unique (there is another treatment with this ID)"),
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
