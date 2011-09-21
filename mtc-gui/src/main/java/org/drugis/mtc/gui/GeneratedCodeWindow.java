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
import org.drugis.mtc.ConsistencyNetworkModel$;
import org.drugis.mtc.InconsistencyNetworkModel$;
import org.drugis.mtc.Network;
import org.drugis.mtc.NetworkModel;
import org.drugis.mtc.RandomizedStartingValueGenerator$;
import org.drugis.mtc.StartingValueGenerator;
import org.drugis.mtc.gui.CodeGenerationDialog.ModelType;
import org.drugis.mtc.gui.CodeGenerationDialog.SyntaxType;
import org.drugis.mtc.jags.JagsSyntaxModel;


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
	
	private final Network<?> d_network;
	private final NetworkModel<?, ?> d_networkModel;
	private final JagsSyntaxModel<?, ?> d_syntaxModel;
	private final SyntaxType d_syntaxType;
	private final ModelType d_modelType;
	private final int d_nchains;
	private final int d_tuning;
	private final int d_simulation;
	private final double d_scale;
	private final String d_name;
	private final String d_suffix;
	private final List<GeneratedFile> d_files;


	public GeneratedCodeWindow(String name, Network<?> network, SyntaxType syntaxType, ModelType modelType,
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
		d_networkModel = buildNetworkModel();
		d_syntaxModel = buildSyntaxModel();
		d_files = buildFiles();
		
		initComponents();
		pack();
	}
	
	@SuppressWarnings("unchecked")
	private List<GeneratedFile> buildFiles() {
		List<GeneratedFile> files = new ArrayList<GeneratedFile>();
		files.add(new GeneratedFile("model", d_syntaxModel.modelText()));
		files.add(new GeneratedFile("data", d_syntaxModel.dataText()));
		StartingValueGenerator gen = RandomizedStartingValueGenerator$.MODULE$.apply(d_networkModel, new JDKRandomGenerator(), d_scale);
		for (int i = 1; i <= d_nchains; ++i) {
			files.add(new GeneratedFile("inits" + i, d_syntaxModel.initialValuesText(gen)));
		}
		files.add(new GeneratedFile("script", d_syntaxModel.scriptText(getBaseName(), d_nchains, d_tuning, d_simulation)));
		return files;
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

	private NetworkModel<?, ?> buildNetworkModel() {
		if (d_modelType == ModelType.Consistency) {
			return ConsistencyNetworkModel$.MODULE$.apply(d_network);
		} else {
			return InconsistencyNetworkModel$.MODULE$.apply(d_network);
		}
	}
	
	@SuppressWarnings("unchecked")
	private JagsSyntaxModel<?, ?> buildSyntaxModel() {
		return new JagsSyntaxModel(d_networkModel, d_syntaxType == SyntaxType.JAGS);
	}
}
