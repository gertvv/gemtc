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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class AboutDialog extends JDialog {
	private static final long serialVersionUID = -1058158913598958769L;

	public AboutDialog(JFrame parent) {
		super(parent, "About " + AppInfo.getAppName() + " " + AppInfo.getAppVersion(), false);
		
		setLocationByPlatform(true);
		initComponents();
		pack();
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		
		Border border = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		
		String title = AppInfo.getAppName() + " " + AppInfo.getAppVersion();
		JLabel titleLabel = new JLabel(title);
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		Font font = titleLabel.getFont();
		font = font.deriveFont(Font.BOLD, 14F);
		titleLabel.setFont(font);
		titleLabel.setBorder(border);
		add(titleLabel, BorderLayout.NORTH);
		
		String aboutText = "GeMTC is open source software for Mixed Treatment Comparison model generation.\n" +
			"Copyright \u00A92009-2012 Gert van Valkenhoef\n\n" +
			"Get GeMTC at <http://drugis.org/mtc>.\n\n" +
			"This program is free software: you can redistribute it and/or modify " +
			"it under the terms of the GNU General Public License as published by " +
			"the Free Software Foundation, either version 3 of the License, or " + 
			"(at your option) any later version.\n\n" + 
			"This program is distributed in the hope that it will be useful, " +
			"but WITHOUT ANY WARRANTY; without even the implied warranty of " +
			"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the " +
			"GNU General Public License for more details.\n\n" +
			"You should have received a copy of the GNU General Public License " +
			"along with this program.  If not, see <http://www.gnu.org/licenses/>.";
		
		JTextArea textArea = new JTextArea(aboutText);
		textArea.setPreferredSize(new Dimension(400, 400));
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		textArea.setOpaque(false);
		textArea.setBorder(border);
		add(textArea, BorderLayout.CENTER);
		
		JLabel logoLabel = new JLabel(MainWindow.IMAGELOADER.getIcon("gemtc.png"));
		logoLabel.setBorder(border);
		add(logoLabel, BorderLayout.EAST);
		
		JButton closeButton = new JButton("Close");
		closeButton.setMnemonic('c');
		closeButton.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 4493415703789106001L;

			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
				dispose();
			}
		});
		add(closeButton, BorderLayout.SOUTH);
	}
}
