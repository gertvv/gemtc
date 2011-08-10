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

import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.BorderFactory;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.list.ObservableList;

import org.drugis.mtc.gui.ListEditor.ListActions;

class TreatmentActions implements ListActions<TreatmentModel> {
	private JFrame d_parent;

	public TreatmentActions(JFrame parent) {
		d_parent = parent;
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
		if (item != null) {
			list.remove(item);
		}
	}

	public String getLabel(TreatmentModel item) {
		return item.getId();
	}
	public String getTooltip(TreatmentModel item) {
		return item.getDescription();
	}

	private void showEditDialog(TreatmentModel model) {
		final JDialog dialog = new JDialog(d_parent, "Treatment");
		dialog.setModal(true);
		//dialog.setMinimumSize(new Dimension(300, 200));

		dialog.setLayout(new BorderLayout());


		JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(new JLabel("ID: "));
		JTextField field1 = BasicComponentFactory.createTextField(new PropertyAdapter<TreatmentModel>(model, TreatmentModel.PROPERTY_ID), false);
		field1.setColumns(25);
		panel.add(field1);
		panel.add(new JLabel("Description: "));
		JTextField field2 = BasicComponentFactory.createTextField(new PropertyAdapter<TreatmentModel>(model, TreatmentModel.PROPERTY_DESCRIPTION), false);
		field2.setColumns(25);
		panel.add(field2);

		dialog.add(panel, BorderLayout.CENTER);

		JButton okButton = new JButton("OK");
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
