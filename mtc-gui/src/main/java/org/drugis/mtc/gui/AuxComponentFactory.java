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

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import org.drugis.common.gui.GUIHelper;

public class AuxComponentFactory {

	public static JPanel createWarningPanel(String warningText) {
		// Get the warning icon from the LookAndFeel, otherwise use a fall back
		Icon icon = UIManager.getIcon("OptionPane.warningIcon");
		if (icon == null) {
			icon = MainWindow.IMAGELOADER.getIcon(FileNames.ICON_WARNING);
		}
		JLabel iconLabel = new JLabel(icon);
	
		// Use a TextPane for auto-wrapping text
		JTextPane textPane = new JTextPane();
		textPane.setText(warningText);
		textPane.setEditable(false);
		textPane.setOpaque(false);
	
		JPanel panel = new JPanel();
		// BoxLayout ensures the components fit the container
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBackground(GUIHelper.COLOR_WARNING);
	
		panel.add(Box.createRigidArea(new Dimension(8, 0))); // padding
		panel.add(iconLabel);
		panel.add(Box.createRigidArea(new Dimension(8, 0))); // padding
		panel.add(textPane);
		panel.add(Box.createRigidArea(new Dimension(8, 0))); // padding
	
		return panel;
	}

}
