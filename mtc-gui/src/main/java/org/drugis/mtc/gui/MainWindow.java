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

public class MainWindow extends JFrame {
	public static void main(String[] args) {
		ImageLoader.setImagePath("/org/drugis/mtc/gui/");
		new MainWindow().setVisible(true);
	}

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

		JList treatmentList = new JList(new String[] { "Fluoxetine", "Paroxetine", "Sertraline" });
		JScrollPane treatmentPane = new JScrollPane(treatmentList);
		
		tabbedPane.addTab("Treatments", null, treatmentPane, "Manage treatments");

		JList studyList = new JList(new String[] { "Chouinard et al 1999", "Fava et al 2002" });
		JScrollPane studyPane = new JScrollPane(studyList);

		tabbedPane.addTab("Studies", null, studyPane, "Manage studies");

		return tabbedPane;
	}

	public JComponent buildDataPane() {
		String [] columnNames = { "", "Responders", "Sample size" };
		String [][] data = {
			{"Chouinard et al 1999", null, null},
			{"Fluoxetine", "37", "101"},
			{"Paroxetine", "35", "102"},
			{"Fava et al 2002", null, null},
			{"Fluoxetine", "38", "88"},
			{"Paroxetine", "40", "99"}
		};
		JTable table = new JTable(data, columnNames);
		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public void setValue(Object value) {
				if (value == null) {
					setText("");
					setBackground(Color.LIGHT_GRAY);
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
