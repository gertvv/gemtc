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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;

import javax.swing.ListModel;

import com.jgoodies.binding.list.ObservableList;
import com.jgoodies.binding.beans.PropertyConnector;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.binding.value.ValueHolder;

public class ListEditor<E> extends JPanel {
	public interface ListActions<T> {
		public String getTypeName();
		public void addAction(ObservableList<T> list);
		public void editAction(ObservableList<T> list, T item);
		public void deleteAction(ObservableList<T> list, T item);
		public String getLabel(T item);
		public String getTooltip(T item);
		public ListModel listPresentation(ObservableList<T> list);
	}

	private ObservableList<E> d_list;
	private ListActions<E> d_actions;

	private ValueModel d_selectedModel = new ValueHolder(false);

	public ListEditor(ObservableList<E> list, ListActions<E> actions) {
		super(new BorderLayout());
		d_list = list;
		d_actions = actions;

		initComponents();
	}

	void initComponents() {
		// The listView
		final JList listView = new JList(d_actions.listPresentation(d_list));
		listView.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			 public Component getListCellRendererComponent(JList list, Object obj, int index, boolean isSelected, boolean cellHasFocus) {
				String label = d_actions.getLabel((E) obj);
				String tooltip = d_actions.getTooltip((E) obj);
				Component component = super.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
				((JComponent)component).setToolTipText(tooltip);
				return component;
			}
		});
		listView.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				d_selectedModel.setValue(listView.getSelectedValue() != null);
			}
		});
		JScrollPane scrollPane = new JScrollPane(listView);
		add(scrollPane, BorderLayout.CENTER);

		// The edit buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		buttonPanel.add(createButton("Add", false, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				d_actions.addAction(d_list);
			}
		}));
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		buttonPanel.add(createButton("Edit", true, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				d_actions.editAction(d_list, (E)listView.getSelectedValue());
			}
		}));
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		buttonPanel.add(createButton("Delete", true, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				d_actions.deleteAction(d_list, (E)listView.getSelectedValue());
			}
		}));
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 3)));
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private JButton createButton(String func, boolean selected, ActionListener listener) {
		JButton button = new JButton(func + " " + d_actions.getTypeName());
		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		button.setMaximumSize(new Dimension(Short.MAX_VALUE, button.getPreferredSize().height));
		button.addActionListener(listener);
		if (selected) {
			PropertyConnector.connectAndUpdate(d_selectedModel, button, "enabled");
		}
		return button;
	}
}
