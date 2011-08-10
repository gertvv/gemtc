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
import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.Box;

public class ListEditor extends JPanel {
	private String d_name;
	private String[] d_list;

	public ListEditor(String name, String [] list) {
		super(new BorderLayout());
		d_name = name;
		d_list = list;

		initComponents();
	}

	void initComponents() {
		// The listView
		JList listView = new JList(d_list);
		JScrollPane scrollPane = new JScrollPane(listView);
		add(scrollPane, BorderLayout.CENTER);

		// The edit buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		buttonPanel.add(createButton("Add"));
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		buttonPanel.add(createButton("Edit"));
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		buttonPanel.add(createButton("Delete"));
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 3)));
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private JButton createButton(String func) {
		JButton button = new JButton(func + " " + d_name);
		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		button.setMaximumSize(new Dimension(Short.MAX_VALUE, button.getPreferredSize().height));
		return button;
	}
}
