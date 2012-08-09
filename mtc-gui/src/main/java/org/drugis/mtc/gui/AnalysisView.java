package org.drugis.mtc.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.drugis.mtc.ConsistencyModel;
import org.drugis.mtc.DefaultModelFactory;
import org.drugis.mtc.gui.results.SimulationComponentFactory;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.presentation.MCMCModelWrapper;
import org.drugis.mtc.presentation.MCMCPresentation;
import org.drugis.mtc.presentation.SimulationConsistencyWrapper;

public class AnalysisView extends JPanel {
	private static final long serialVersionUID = -3923180226772918488L;
	
	private final JFrame d_parent;
	private final AnalysesModel d_analyses = new AnalysesModel();
	private final DataSetModel d_dataset;
	private JSplitPane d_mainPane;

	public AnalysisView(JFrame parent, DataSetModel model) {
		d_parent = parent;
		d_dataset = model;
		setLayout(new BorderLayout());
		initComponents();
	}

	private void initComponents() {
		JTree tree = new JTree(d_analyses);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				TreePath path = e.getPath();
				if (path.getLastPathComponent() instanceof MCMCPresentation) {
					d_mainPane.setRightComponent(buildModelPanel((MCMCPresentation)path.getLastPathComponent()));
				} else if (path.getLastPathComponent() instanceof ModelType) {
					d_mainPane.setRightComponent(buildTypePanel((ModelType)path.getLastPathComponent()));
				} else {
					d_mainPane.setRightComponent(buildRootPanel());
				}
			}
		});
		tree.setEditable(false);
		
		JScrollPane treePane = new JScrollPane(tree);
		JPanel viewPane = buildRootPanel();
		JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePane, viewPane);
		mainPane.setDividerLocation(200);
		add(mainPane, BorderLayout.CENTER);
		d_mainPane = mainPane;
	}

	private void generateModels() {
		Network network = d_dataset.getNetwork();
		ConsistencyModel model = DefaultModelFactory.instance().getConsistencyModel(network);
		MCMCModelWrapper wrapper = new SimulationConsistencyWrapper<Treatment>(
				model,
				network.getTreatments(),
				Util.identityMap(network.getTreatments()));
		MCMCPresentation presentation = new MCMCPresentation(wrapper, "Consistency model");
		d_analyses.add(ModelType.Consistency, presentation);
	}

	private JPanel buildRootPanel() {
		JPanel viewPane = new JPanel();
		final JButton button = new JButton("Generate models");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				d_analyses.clear();
				generateModels();
			}
		});
		viewPane.add(button);
		return viewPane;
	}

	private Component buildTypePanel(ModelType type) {
		return new JLabel(type.toString());
	}

	private JPanel buildModelPanel(MCMCPresentation presentation) {
		return SimulationComponentFactory.createSimulationControls(presentation, d_parent, false, null, null);
	}
}
