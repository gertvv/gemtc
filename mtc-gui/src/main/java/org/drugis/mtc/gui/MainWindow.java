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
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JSplitPane;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;

import org.drugis.common.ImageLoader;

import com.jgoodies.binding.list.ObservableList;
import com.jgoodies.binding.list.ArrayListModel;
import java.util.Arrays;

import org.drugis.mtc.gui.ListEditor.ListActions;

public class MainWindow extends JFrame {
	public static void main(String[] args) {
		ImageLoader.setImagePath("/org/drugis/mtc/gui/");
		new MainWindow().setVisible(true);
	}

	ObservableList<TreatmentModel> d_treatments;
	ObservableList<StudyModel> d_studies;

	public MainWindow() {
		super("drugis.org MTC");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		setMinimumSize(new Dimension(750, 550));

		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		initToolBar();
		JComponent entityPane = buildEntityPane();
		JComponent infoPane = buildInfoPane();
		JComponent dataPane = buildDataPane();
		JPanel rightPane = new JPanel(new BorderLayout());
		rightPane.add(infoPane, BorderLayout.NORTH);
		rightPane.add(dataPane, BorderLayout.CENTER);
		JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, entityPane, rightPane);
		mainPane.setDividerLocation(200);
		add(mainPane, BorderLayout.CENTER);
	}

	private void initToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

		toolbar.add(new JButton("New", ImageLoader.getIcon("newfile.gif")));
		toolbar.add(new JButton("Open", ImageLoader.getIcon("openfile.gif")));
		toolbar.add(new JButton("Save", ImageLoader.getIcon("savefile.gif")));
		toolbar.add(new JButton("Generate", ImageLoader.getIcon("generate.gif")));

        add(toolbar, BorderLayout.NORTH);
	}

	private JComponent buildEntityPane() {
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setMinimumSize(new Dimension(200, 400));
		tabbedPane.setTabPlacement(JTabbedPane.TOP);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		TreatmentModel fluox = new TreatmentModel();
		fluox.setId("Fluox");
		fluox.setDescription("Fluoxetine");
		TreatmentModel parox = new TreatmentModel();
		parox.setId("Parox");
		parox.setDescription("Paroxetine");
		ObservableList<TreatmentModel> treatmentList = new ArrayListModel<TreatmentModel>(
			Arrays.asList(fluox, parox));
		d_treatments = treatmentList;
		StudyModel ch = new StudyModel();
		ch.setId("Chouinard et al 1999");
		ch.getTreatments().addAll(treatmentList);
		StudyModel fa = new StudyModel();
		fa.setId("Fava et al 2002");
		fa.getTreatments().addAll(treatmentList);
		ObservableList<StudyModel> studyList = new ArrayListModel<StudyModel>(Arrays.asList(ch, fa));
		d_studies = studyList;

		JComponent treatmentPane = new ListEditor(treatmentList, new TreatmentActions(this, studyList));
		tabbedPane.addTab("Treatments", null, treatmentPane, "Manage treatments");

		JComponent studyPane = new ListEditor(studyList, new StudyActions(this, treatmentList));
		tabbedPane.addTab("Studies", null, studyPane, "Manage studies");

		return tabbedPane;
	}

	public JComponent buildDataPane() {
		JTable table = new JTable(new MeasurementTableModel(d_studies));
		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public void setValue(Object value) {
				if (value == null) {
					setText("");
					setBackground(Color.LIGHT_GRAY);
				} else if (value instanceof StudyModel) {
					setText(((StudyModel)value).getId());
					setBackground(Color.LIGHT_GRAY);
				} else if (value instanceof TreatmentModel) {
					setText(((TreatmentModel)value).getId());
					setBackground(Color.WHITE);
				} else {
					setText(value.toString());
					setBackground(Color.WHITE);
				}
			}
		});
		return new JScrollPane(table);
	}

	public JComponent buildInfoPane() {
		JPanel panel = new JPanel(new FlowLayout());
		panel.add(new JLabel("A "));
		panel.add(new JComboBox(new String[] { "dichotomous", "continuous", "(none)" }));
		panel.add(new JLabel(" dataset about "));
		panel.add(new JTextField("HAM-D responders at 8 weeks (severe depression)", 20));
		return panel;
	}
}
