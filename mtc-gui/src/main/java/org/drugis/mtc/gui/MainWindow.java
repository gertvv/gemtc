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
import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import org.drugis.common.ImageLoader;
import org.drugis.mtc.Measurement;
import org.drugis.mtc.Network;

public class MainWindow extends JFrame {
	private static final long serialVersionUID = -5199299195474870618L;

	public static void main(String[] args) {
		ImageLoader.setImagePath("/org/drugis/mtc/gui/");
		new MainWindow().setVisible(true);
	}

	DataSetModel d_model;

	public MainWindow() {
		super("drugis.org MTC");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		setMinimumSize(new Dimension(750, 550));
		initDataSet();

		initComponents();
	}

	private void initDataSet() {
//		InputStream is = MainWindow.class.getResourceAsStream("luades-smoking.xml");
		InputStream is;
		try {
			is = new FileInputStream("/home/gert/Documents/repositories/mtc/mtc-gui/src/main/resources/org/drugis/mtc/gui/luades-smoking.xml");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		Network<? extends Measurement> network = Network.fromXML(scala.xml.XML.load(is));
		d_model = DataSetModel.build(network);
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		initToolBar();

		JComponent mainPane = new DataSetView(this, d_model);
		add(mainPane , BorderLayout.CENTER);
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


}
