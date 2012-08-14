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
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.drugis.common.beans.ContentAwareListModel;
import org.drugis.common.validation.ListMinimumSizeModel;
import org.drugis.common.validation.PropertyUniqueModel;
import org.drugis.common.validation.StringNotEmptyModel;
import org.drugis.mtc.gui.ListEditor.ListActions;
import org.drugis.mtc.gui.ValidationPanel.Validation;
import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.NetworkModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.list.ObservableList;
import com.jgoodies.binding.value.AbstractValueModel;
import com.jgoodies.binding.value.ValueModel;

class StudyActions implements ListActions<Study> {
	private JFrame d_parent;
	private ObservableList<Treatment> d_treatments;

	private static class TreatmentSelectedModel extends AbstractValueModel {
		private static final long serialVersionUID = -8538914749123577488L;

		private Study d_study;
		private Treatment d_treatment;

		public TreatmentSelectedModel(Study study, Treatment treatment) {
			d_study = study;
			d_treatment = treatment;
		}

		public Boolean getValue() {
			return NetworkModel.findMeasurement(d_study, d_treatment) != null;
		}

		public void setValue(Object obj) {
			boolean oldVal = getValue();
			boolean val = (Boolean)obj;
			if (val && !oldVal) {
				d_study.getMeasurements().add(new Measurement(d_treatment));
			} else if (!val && oldVal) {
				d_study.getMeasurements().remove(NetworkModel.findMeasurement(d_study, d_treatment));
			}
			fireValueChange(oldVal, val);
		}
	}

	public StudyActions(JFrame parent, ObservableList<Treatment> treatments) {
		d_parent = parent;
		d_treatments = treatments;
	}

	public String getTypeName() {
		return "study";
	}

	public void addAction(ObservableList<Study> list) {
		if (d_treatments.size() < 2) {
			JOptionPane.showMessageDialog(d_parent,
				"Before you can create any studies, you have to create at least two treatments.",
				"Unable to create study", JOptionPane.WARNING_MESSAGE);
			return;
		}
		final Study model = new Study();
		addDefaultValueInserter(model);
		showEditDialog(list, model);
		list.add(model);
	}

	public void editAction(ObservableList<Study> list, Study item) {
		if (item != null) {
			showEditDialog(list, item);
		}
	}

	public void deleteAction(ObservableList<Study> list, Study item) {
		if (item != null) {
			list.remove(item);
		}
	}

	public String getLabel(Study item) {
		return item.getId();
	}
	public String getTooltip(Study item) {
		return item.getId();
	}

	public ListModel listPresentation(ObservableList<Study> list) {
		return new ContentAwareListModel<Study>(list, new String[] { Study.PROPERTY_ID });
	}

	public static void addDefaultValueInserter(final Study model) {
		model.getMeasurements().addListDataListener(new ListDataListener() { // FIXME: HACK
			public void intervalRemoved(ListDataEvent e) {
			}
			
			public void intervalAdded(ListDataEvent e) {
				for (int i = e.getIndex0(); i <= e.getIndex1(); ++i) {
					model.getMeasurements().get(i).setSampleSize(0);
					model.getMeasurements().get(i).setResponders(0);
					model.getMeasurements().get(i).setMean(0.0);
					model.getMeasurements().get(i).setStdDev(0.0);
				}
			}
			
			public void contentsChanged(ListDataEvent e) {
			}
		});
	}
	
	private void showEditDialog(ObservableList<Study> list, Study model) {
		final JDialog dialog = new JDialog(d_parent, "Study");
		dialog.setLocationByPlatform(true);
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		//dialog.setMinimumSize(new Dimension(300, 200));

		dialog.setLayout(new BorderLayout());


		JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(new JLabel("ID: "));
		ValueModel idModel = new PropertyAdapter<Study>(model, Study.PROPERTY_ID, true);
		JTextField field1 = BasicComponentFactory.createTextField(idModel, false);
		field1.setColumns(25);
		panel.add(field1);
		dialog.add(panel, BorderLayout.NORTH);

		JPanel treatmentPanel = new JPanel(new GridLayout(0, 3, 5, 5));
		for (Treatment t : d_treatments) {
			treatmentPanel.add(BasicComponentFactory.createCheckBox(new TreatmentSelectedModel(model, t), t.getId()));
		}
		dialog.add(treatmentPanel, BorderLayout.CENTER);

		ValueModel idNotEmpty = new StringNotEmptyModel(idModel);
		ValueModel idUnique = new PropertyUniqueModel<Study>(list, model, Study.PROPERTY_ID);
		ValueModel treatmentsSelected = new ListMinimumSizeModel(model.getMeasurements(), 2);
		List<Validation> validations = Arrays.asList(
				new Validation(idNotEmpty, "The ID may not be empty"),
				new Validation(idUnique, "The ID must be unique (there is another treatment with this ID)"),
				new Validation(treatmentsSelected, "You must select at least two treatments")
				);

		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				dialog.dispose();
			}
		};
		JPanel bottomPanel = new ValidationPanel(validations, okListener);
		dialog.add(bottomPanel, BorderLayout.SOUTH);

		dialog.pack();
		dialog.setVisible(true);
	}
}

