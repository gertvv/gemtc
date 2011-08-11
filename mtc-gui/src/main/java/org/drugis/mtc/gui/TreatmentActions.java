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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.WindowConstants;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;

import javax.swing.ListModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.beans.PropertyConnector;
import com.jgoodies.binding.list.ObservableList;
import com.jgoodies.binding.value.ValueModel;

import org.drugis.mtc.gui.ListEditor.ListActions;

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
		showEditDialog(model);
		list.add(model);
	}
	public void editAction(ObservableList<TreatmentModel> list, TreatmentModel item) {
		if (item != null) {
			showEditDialog(item);
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
		return new ContentAwareListModel(list, new String[] { TreatmentModel.PROPERTY_ID, TreatmentModel.PROPERTY_DESCRIPTION });
	}

	private void showEditDialog(TreatmentModel model) {
		final JDialog dialog = new JDialog(d_parent, "Treatment");
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		//dialog.setMinimumSize(new Dimension(300, 200));

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

		JButton okButton = new JButton("OK");
		PropertyConnector.connectAndUpdate(new StringNotEmptyModel(idModel), okButton, "enabled");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				dialog.dispose();
			}
		});
		dialog.add(okButton, BorderLayout.SOUTH);

		dialog.pack();
		dialog.setVisible(true);
	}
}
