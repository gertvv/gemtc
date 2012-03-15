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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import org.apache.commons.math.random.JDKRandomGenerator;
import org.drugis.common.ImageLoader;
import org.drugis.mtc.gui.CodeGenerationDialog.ModelType;
import org.drugis.mtc.gui.CodeGenerationDialog.SyntaxType;
import org.drugis.mtc.jags.JagsSyntaxModel;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.parameterization.AbstractDataStartingValueGenerator;
import org.drugis.mtc.parameterization.ConsistencyParameterization;
import org.drugis.mtc.parameterization.InconsistencyParameterization;
import org.drugis.mtc.parameterization.NetworkModel;
import org.drugis.mtc.parameterization.Parameterization;
import org.drugis.mtc.parameterization.StartingValueGenerator;


public class GeneratedCodeWindow extends JFrame {
	private static final long serialVersionUID = -5399697448245239245L;

	private class GeneratedFile {
		private final String extension;
		private final String text;
		
		public GeneratedFile(String extension, String text) {
			this.extension = extension;
			this.text = text;
		}
	}
	
	private final Network d_network;
	private final Parameterization d_pmtz;
	private final JagsSyntaxModel d_syntaxModel;
	private final SyntaxType d_syntaxType;
	private final ModelType d_modelType;
	private final int d_nchains;
	private final int d_tuning;
	private final int d_simulation;
	private final double d_scale;
	private final String d_name;
	private final String d_suffix;
	private final List<GeneratedFile> d_files;


	public GeneratedCodeWindow(String name, Network network, SyntaxType syntaxType, ModelType modelType,
			int nchains, int tuning, int simulation, double scale) {
		super(syntaxType.toString() + " " + modelType.toString() + " model: " + name);
		d_name = name;
		d_network = network;
		d_syntaxType = syntaxType;
		d_modelType = modelType;
		d_nchains = nchains;
		d_tuning = tuning;
		d_simulation = simulation;
		d_scale = scale;
		d_suffix = buildSuffix();
		d_pmtz = buildParameterization();
		d_syntaxModel = buildSyntaxModel();
		d_files = buildFiles();
		
		initComponents();
		pack();
	}
	
	private List<GeneratedFile> buildFiles() {
		List<GeneratedFile> files = new ArrayList<GeneratedFile>();
		files.add(new GeneratedFile("model", d_syntaxModel.modelText()));
		files.add(new GeneratedFile("data", d_syntaxModel.dataText()));
		StartingValueGenerator gen = buildStartingValueGenerator();
		for (int i = 1; i <= d_nchains; ++i) {
			files.add(new GeneratedFile("inits" + i, d_syntaxModel.initialValuesText(gen)));
		}
		files.add(new GeneratedFile("script", d_syntaxModel.scriptText(getBaseName(), d_nchains, d_tuning, d_simulation)));
		if (d_syntaxType.equals(SyntaxType.JAGS)) {
			files.add(new GeneratedFile("R", d_syntaxModel.analysisText(getBaseName())));
		}
		return files;
	}

	private StartingValueGenerator buildStartingValueGenerator() {
		return AbstractDataStartingValueGenerator.create(d_network, NetworkModel.createComparisonGraph(d_network), new JDKRandomGenerator(), d_scale);
	}

	private String buildSuffix() {
		if (d_modelType == ModelType.Consistency) {
			return "cons";
		} else {
			return "inco";
		}
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		
		add(createToolBar(), BorderLayout.NORTH);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		for (GeneratedFile file : d_files) {
			tabbedPane.addTab(file.extension, new JScrollPane(new JTextArea(file.text)));
		}
		add(tabbedPane, BorderLayout.CENTER);
	}
	
	
	private String getBaseName() {
		return d_name + "." + d_suffix;
	}

	protected void writeFiles(File dir) throws IOException {
		for (GeneratedFile file : d_files) {
			String filePath = dir.getPath() + "/" + getBaseName() + "." + file.extension;
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filePath));
			osw.write(file.text);
			osw.close();
		}
	}
	
	private JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

		toolbar.add(createSaveButton());

        return toolbar;
	}

	private JButton createSaveButton() {
		JButton saveButton = new JButton("Save", ImageLoader.getIcon("savefile.gif"));
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					JFileChooser chooser = new JFileChooser();
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					
					int returnVal = chooser.showSaveDialog(GeneratedCodeWindow.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = chooser.getSelectedFile();
						if (!file.isDirectory()) {
							JOptionPane.showMessageDialog(GeneratedCodeWindow.this, "Error: please select a directory to save to", "Files could not be saved.", JOptionPane.ERROR_MESSAGE);
						}
						writeFiles(file);
						JOptionPane.showMessageDialog(GeneratedCodeWindow.this, "Your files have been saved to: \n" + file.getPath(), "Files saved.", JOptionPane.INFORMATION_MESSAGE);
					}
				} catch (IOException e) {
					JOptionPane.showMessageDialog(GeneratedCodeWindow.this, "Error: " + e.getMessage(), "Files could not be saved.", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		return saveButton;
	}

	private Parameterization buildParameterization() {
		if (d_modelType == ModelType.Consistency) {
			return ConsistencyParameterization.create(d_network);
		} else {
			return InconsistencyParameterization.create(d_network);
		}
	}
	
	private JagsSyntaxModel buildSyntaxModel() {
		return new JagsSyntaxModel(d_network, d_pmtz, d_syntaxType == SyntaxType.JAGS);
	}
}
