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

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.drugis.mtc.ConsistencyModel;
import org.drugis.mtc.InconsistencyModel;
import org.drugis.mtc.MixedTreatmentComparison;
import org.drugis.mtc.NodeSplitModel;
import org.drugis.mtc.jags.JagsSyntaxModel;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.parameterization.AbstractDataStartingValueGenerator;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.parameterization.ConsistencyParameterization;
import org.drugis.mtc.parameterization.InconsistencyParameterization;
import org.drugis.mtc.parameterization.NetworkModel;
import org.drugis.mtc.parameterization.NodeSplitParameterization;
import org.drugis.mtc.parameterization.Parameterization;
import org.drugis.mtc.parameterization.StartingValueGenerator;
import org.drugis.mtc.presentation.MTCModelWrapper;


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

	public enum SyntaxType {
		BUGS,
		JAGS
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
	private final BasicParameter d_splitNode;


	public GeneratedCodeWindow(String name, Network network, SyntaxType syntaxType, ModelType modelType,
			BasicParameter splitNode, int nchains, int tuning, int simulation, double scale) {
		super(syntaxType.toString() + " " + modelType.toString() + " model: " + name);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		d_name = name;
		d_network = network;
		d_syntaxType = syntaxType;
		d_modelType = modelType;
		d_splitNode = splitNode;
		d_nchains = nchains;
		d_tuning = tuning;
		d_simulation = simulation;
		d_scale = scale;
		d_suffix = buildSuffix();
		d_pmtz = buildParameterization();
		d_syntaxModel = buildSyntaxModel();
		d_files = buildFiles();

		MainWindow.setAppIcon(this);

		initComponents();
		pack();
	}

	public GeneratedCodeWindow(SyntaxType syntaxType, Network network, MTCModelWrapper<?> model) {
		this(model.getDescription(), network, syntaxType, getType(model.getModel()), getSplitNode(model.getModel()),
				model.getSettings().getNumberOfChains(), model.getSettings().getTuningIterations(),
				model.getSettings().getSimulationIterations(), model.getSettings().getVarianceScalingFactor());
	}

	private static BasicParameter getSplitNode(MixedTreatmentComparison model) {
		if (getType(model).equals(ModelType.NodeSplit)) {
			return ((NodeSplitModel)model).getSplitNode();
		}
		return null;
	}

	private static ModelType getType(MixedTreatmentComparison model) {
		if (model instanceof ConsistencyModel) {
			return ModelType.Consistency;
		}
		if (model instanceof InconsistencyModel) {
			return ModelType.Inconsistency;
		}
		if (model instanceof NodeSplitModel) {
			return ModelType.NodeSplit;
		}
		throw new IllegalStateException("Unknown type " + model.getClass().getSimpleName());
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
		switch (d_modelType) {
		case Consistency:
			return "cons";
		case Inconsistency:
			return "inco";
		case NodeSplit:
			return "splt." + d_splitNode.getBaseline().getId() + "." + d_splitNode.getSubject().getId();
		}
		throw new IllegalStateException("Unhandled model type + " + d_modelType);
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
		JButton saveButton = new JButton("Save", MainWindow.IMAGELOADER.getIcon("savefile.gif"));
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
		switch (d_modelType) {
		case Consistency:
			return ConsistencyParameterization.create(d_network);
		case Inconsistency:
			return InconsistencyParameterization.create(d_network);
		case NodeSplit:
			return NodeSplitParameterization.create(d_network, d_splitNode);
		}
		throw new IllegalStateException("Unhandled model type + " + d_modelType);
	}

	private JagsSyntaxModel buildSyntaxModel() {
		return new JagsSyntaxModel(d_network, d_pmtz, d_syntaxType == SyntaxType.JAGS);
	}
}

