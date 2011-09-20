package org.drugis.mtc.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import org.apache.commons.math.random.JDKRandomGenerator;
import org.drugis.common.ImageLoader;
import org.drugis.common.gui.FileDialog;
import org.drugis.common.gui.FileSaveDialog;
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
		d_networkModel = buildNetworkModel();
		d_syntaxModel = buildSyntaxModel();
		
		initComponents();
		pack();
	}
	
	private void initComponents() {
		setLayout(new BorderLayout());
		
		add(createToolBar(), BorderLayout.NORTH);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		
		tabbedPane.addTab("Model", new JScrollPane(new JTextArea(d_syntaxModel.modelText())));
		tabbedPane.addTab("Data", new JScrollPane(new JTextArea(d_syntaxModel.dataText())));
		final int chains = d_nchains;
		StartingValueGenerator gen = RandomizedStartingValueGenerator$.MODULE$.apply(d_networkModel, new JDKRandomGenerator(), d_scale);
		for (int i = 1; i <= chains; ++i) {
			tabbedPane.addTab("Inits " + i, new JScrollPane(new JTextArea(d_syntaxModel.initialValuesText(gen))));
		}
		tabbedPane.addTab("Script", new JScrollPane(new JTextArea(d_syntaxModel.scriptText(d_name, chains, d_tuning, d_simulation))));

		add(tabbedPane, BorderLayout.CENTER);
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
//					JFileChooser chooser = new JFileChooser();
//					chooser.set
						FileDialog dialog = new FileSaveDialog(GeneratedCodeWindow.this, "xml", "XML files") {
							public void doAction(String path, String extension) {
								File file = new File(path);
//								writeToFile(model, file);
//								model.setFile(file);
							}
						};
						dialog.setVisible(true);
				} catch (IllegalArgumentException e) {
					JOptionPane.showMessageDialog(GeneratedCodeWindow.this, "Error: " + e.getMessage(), "File(s) could not be saved.", JOptionPane.ERROR_MESSAGE);
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
