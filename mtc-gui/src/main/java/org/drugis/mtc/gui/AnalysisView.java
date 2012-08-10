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
import org.drugis.mtc.InconsistencyModel;
import org.drugis.mtc.NodeSplitModel;
import org.drugis.mtc.data.DataType;
import org.drugis.mtc.gui.results.ConsistencyView;
import org.drugis.mtc.gui.results.SimulationComponentFactory;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.presentation.ConsistencyWrapper;
import org.drugis.mtc.presentation.MCMCModelWrapper;
import org.drugis.mtc.presentation.MCMCPresentation;
import org.drugis.mtc.presentation.SimulationConsistencyWrapper;
import org.drugis.mtc.presentation.SimulationInconsistencyWrapper;
import org.drugis.mtc.presentation.SimulationNodeSplitWrapper;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class AnalysisView extends JPanel {
	private static final long serialVersionUID = -3923180226772918488L;
	
	private final JFrame d_parent;
	private final DataSetModel d_dataset;
	private final AnalysesModel d_analyses = new AnalysesModel();
	private Network d_network;
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
					d_mainPane.setRightComponent(new JScrollPane(buildModelPanel((MCMCPresentation)path.getLastPathComponent())));
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
		d_network = d_dataset.getNetwork(); // Cache for when the view is generated later
		d_analyses.add(ModelType.Consistency, buildConsistencyModel());
		d_analyses.add(ModelType.Inconsistency, buildInconsistencyModel());
		for (BasicParameter node : DefaultModelFactory.instance().getSplittableNodes(d_network)) {
			d_analyses.add(ModelType.NodeSplit, buildNodeSplitModel(node));
		}
	}

	private MCMCPresentation buildConsistencyModel() {
		ConsistencyModel model = DefaultModelFactory.instance().getConsistencyModel(d_network);
		MCMCModelWrapper wrapper = new SimulationConsistencyWrapper<Treatment>(
				model,
				d_network.getTreatments(),
				Util.identityMap(d_network.getTreatments()));
		MCMCPresentation presentation = new MCMCPresentation(wrapper, "Consistency model");
		return presentation;
	}
	
	private MCMCPresentation buildInconsistencyModel() {
		InconsistencyModel model = DefaultModelFactory.instance().getInconsistencyModel(d_network);
		MCMCModelWrapper wrapper = new SimulationInconsistencyWrapper<Treatment>(
				model,
				Util.identityMap(d_network.getTreatments()));
		MCMCPresentation presentation = new MCMCPresentation(wrapper, "Inconsistency model");
		return presentation;
	}
	
	private MCMCPresentation buildNodeSplitModel(BasicParameter split) {
		NodeSplitModel model = DefaultModelFactory.instance().getNodeSplitModel(d_network, split);
		MCMCModelWrapper wrapper = new SimulationNodeSplitWrapper<Treatment>(
				model,
				Util.identityMap(d_network.getTreatments()));
		MCMCPresentation presentation = new MCMCPresentation(wrapper, split.getName());
		return presentation;
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
		JPanel controls = SimulationComponentFactory.createSimulationControls(presentation, d_parent, true, null, null);
		JPanel results = new JPanel();
		if (presentation.getModel() instanceof ConsistencyModel) {
			results = new ConsistencyView(d_network.getTreatments(), (ConsistencyWrapper<?>)presentation.getWrapper(), d_network.getType().equals(DataType.RATE));
		} else if (presentation.getModel() instanceof InconsistencyModel) {
			
		} else if (presentation.getModel() instanceof NodeSplitModel) {
			
		}
		
		CellConstraints cc = new CellConstraints();
		FormLayout layout = new FormLayout("pref:grow:fill", "p, 3dlu, p");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.add(controls, cc.xy(1, 1));
		builder.add(results, cc.xy(1, 3));
		return builder.getPanel();
	}
}
