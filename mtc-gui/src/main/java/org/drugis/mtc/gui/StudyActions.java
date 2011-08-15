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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.WindowConstants;

import org.drugis.mtc.gui.ListEditor.ListActions;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.beans.PropertyConnector;
import com.jgoodies.binding.list.ObservableList;
import com.jgoodies.binding.value.AbstractValueModel;
import com.jgoodies.binding.value.ValueModel;

class StudyActions implements ListActions<StudyModel> {
	private JFrame d_parent;
	private ObservableList<TreatmentModel> d_treatments;

	private static class TreatmentSelectedModel extends AbstractValueModel {
		private static final long serialVersionUID = -8538914749123577488L;

		private StudyModel d_study;
		private TreatmentModel d_treatment;

		public TreatmentSelectedModel(StudyModel study, TreatmentModel treatment) {
			d_study = study;
			d_treatment = treatment;
		}

		public Boolean getValue() {
			return d_study.getTreatments().contains(d_treatment);
		}

		public void setValue(Object obj) {
			boolean oldVal = getValue();
			boolean val = (Boolean)obj;
			if (val && !oldVal) {
				d_study.getTreatments().add(d_treatment);
			} else if (!val && oldVal) {
				d_study.getTreatments().remove(d_treatment);
			}
			fireValueChange(oldVal, val);
		}
	}

	public StudyActions(JFrame parent, ObservableList<TreatmentModel> treatments) {
		d_parent = parent;
		d_treatments = treatments;
	}

	public String getTypeName() {
		return "study";
	}

	public void addAction(ObservableList<StudyModel> list) {
		if (d_treatments.size() < 2) {
			JOptionPane.showMessageDialog(d_parent,
				"Before you can create any studies, you have to create at least two treatments.",
				"Unable to create study", JOptionPane.WARNING_MESSAGE);
			return;
		}
		StudyModel model = new StudyModel();
		showEditDialog(list, model);
		list.add(model);
	}
	public void editAction(ObservableList<StudyModel> list, StudyModel item) {
		if (item != null) {
			showEditDialog(list, item);
		}
	}
	public void deleteAction(ObservableList<StudyModel> list, StudyModel item) {
		if (item != null) {
			list.remove(item);
		}
	}

	public String getLabel(StudyModel item) {
		return item.getId();
	}
	public String getTooltip(StudyModel item) {
		return item.getId();
	}

	public ListModel listPresentation(ObservableList<StudyModel> list) {
		return new ContentAwareListModel<StudyModel>(list, new String[] { StudyModel.PROPERTY_ID });
	}

	private void showEditDialog(ObservableList<StudyModel> list, StudyModel model) {
		final JDialog dialog = new JDialog(d_parent, "Study");
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		//dialog.setMinimumSize(new Dimension(300, 200));

		dialog.setLayout(new BorderLayout());


		JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(new JLabel("ID: "));
		ValueModel idModel = new PropertyAdapter<StudyModel>(model, StudyModel.PROPERTY_ID, true);
		JTextField field1 = BasicComponentFactory.createTextField(idModel, false);
		field1.setColumns(25);
		panel.add(field1);
		dialog.add(panel, BorderLayout.NORTH);

		JPanel treatmentPanel = new JPanel(new GridLayout(0, 3, 5, 5));
		for (TreatmentModel t : d_treatments) {
			treatmentPanel.add(BasicComponentFactory.createCheckBox(new TreatmentSelectedModel(model, t), t.getId()));
		}
		dialog.add(treatmentPanel, BorderLayout.CENTER);

		ValueModel idNotEmpty = new StringNotEmptyModel(idModel);
		ValueModel idUnique = new PropertyUniqueModel<StudyModel>(list, model, StudyModel.PROPERTY_ID);
		ValueModel treatmentsSelected = new ListMinimumSizeModel(model.getTreatments(), 2);
		ValueModel complete = new BooleanAndModel(treatmentsSelected, new BooleanAndModel(idNotEmpty, idUnique));

		JButton okButton = new JButton("OK");
		PropertyConnector.connectAndUpdate(complete, okButton, "enabled");
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

