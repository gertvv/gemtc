package org.drugis.mtc.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.tree.TreePath;

import org.drugis.common.gui.LayoutUtil;
import org.drugis.common.gui.table.EnhancedTable;
import org.drugis.common.gui.table.TablePanel;
import org.drugis.mtc.ConsistencyModel;
import org.drugis.mtc.DefaultModelFactory;
import org.drugis.mtc.InconsistencyModel;
import org.drugis.mtc.NodeSplitModel;
import org.drugis.mtc.data.DataType;
import org.drugis.mtc.gui.CodeGenerationDialog.SyntaxType;
import org.drugis.mtc.gui.results.ConsistencyView;
import org.drugis.mtc.gui.results.InconsistencyView;
import org.drugis.mtc.gui.results.NodeSplitView;
import org.drugis.mtc.gui.results.ResultsComponentFactory;
import org.drugis.mtc.gui.results.SimulationComponentFactory;
import org.drugis.mtc.gui.results.SummaryCellRenderer;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.presentation.ConsistencyWrapper;
import org.drugis.mtc.presentation.InconsistencyWrapper;
import org.drugis.mtc.presentation.MCMCModelWrapper;
import org.drugis.mtc.presentation.MCMCPresentation;
import org.drugis.mtc.presentation.MTCModelWrapper;
import org.drugis.mtc.presentation.NodeSplitWrapper;
import org.drugis.mtc.presentation.SimulationConsistencyWrapper;
import org.drugis.mtc.presentation.SimulationInconsistencyWrapper;
import org.drugis.mtc.presentation.SimulationNodeSplitWrapper;
import org.drugis.mtc.presentation.results.NodeSplitResultsTableModel;
import org.drugis.mtc.summary.NodeSplitPValueSummary;
import org.drugis.mtc.summary.QuantileSummary;

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

	private MCMCPresentation d_consistency;

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
		treePane.setMinimumSize(new Dimension(150, 100));
		JPanel viewPane = buildRootPanel();
		JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePane, viewPane);
		add(mainPane, BorderLayout.CENTER);
		d_mainPane = mainPane;
	}

	private void generateModels() {
		d_network = d_dataset.getNetwork(); // Cache for when the view is generated later
		d_consistency = buildConsistencyModel();
		d_analyses.add(ModelType.Consistency, d_consistency);
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
		CellConstraints cc = new CellConstraints();
		FormLayout layout = new FormLayout("pref:grow:fill", "p");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		switch (type) {
		case Consistency:
			builder.addSeparator("Consistency model", cc.xy(1, 1));
			break;
		case Inconsistency:
			builder.addSeparator("Inconsistency model", cc.xy(1, 1));
			break;
		case NodeSplit:
			builder.addSeparator("Node splitting models", cc.xy(1, 1));
			LayoutUtil.addRow(layout);
			List<NodeSplitWrapper<?>> wrappers = new ArrayList<NodeSplitWrapper<?>>();
			for (MCMCPresentation p : d_analyses.getModels(ModelType.NodeSplit)) {
				wrappers.add((NodeSplitWrapper<?>) p.getWrapper());
			}
			TableModel model = new NodeSplitResultsTableModel((ConsistencyWrapper<?>) d_consistency.getWrapper(), wrappers, false);
			EnhancedTable table = EnhancedTable.createBare(model);
			table.setDefaultRenderer(QuantileSummary.class, new SummaryCellRenderer(false));
			table.setDefaultRenderer(NodeSplitPValueSummary.class, new SummaryCellRenderer(false));
			table.autoSizeColumns();
			builder.add(new TablePanel(table), cc.xy(1, 3));
			break;
		default:
			break;
		}

		return builder.getPanel();
	}

	private JPanel buildModelPanel(MCMCPresentation presentation) {
		JPanel results = new JPanel();
		if (presentation.getModel() instanceof ConsistencyModel) {
			results = new ConsistencyView(d_network.getTreatments(), (ConsistencyWrapper<?>)presentation.getWrapper(), d_network.getType().equals(DataType.RATE));
		} else if (presentation.getModel() instanceof InconsistencyModel) {
			results = new InconsistencyView(d_network.getTreatments(), (InconsistencyWrapper<?>)presentation.getWrapper(), d_network.getType().equals(DataType.RATE));
		} else if (presentation.getModel() instanceof NodeSplitModel) {
			NodeSplitModel nodeSplitModel = (NodeSplitModel) presentation.getModel();
			results = new NodeSplitView(nodeSplitModel.getSplitNode(), (NodeSplitWrapper<?>)presentation.getWrapper(), (ConsistencyWrapper<?>) d_consistency.getWrapper());
		}

		CellConstraints cc = new CellConstraints();
		FormLayout layout = new FormLayout("pref:grow:fill", "p, 3dlu, p, 3dlu, p");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.add(SimulationComponentFactory.createSimulationControls(presentation, d_parent, true, null, null), cc.xy(1, 1));
		builder.add(buildToolsPanel((MTCModelWrapper<?>) presentation.getWrapper(), d_parent), cc.xy(1, 3));
		builder.add(results, cc.xy(1, 5));
		return builder.getPanel();
	}

	private Component buildToolsPanel(MTCModelWrapper<?> model, JFrame parent) {
		CellConstraints cc = new CellConstraints();
		FormLayout layout = new FormLayout(
				"left:pref, 3dlu, left:pref, 3dlu, pref, 3dlu, pref, fill:0:grow",
				"p, 3dlu, p"
				);
		PanelBuilder builder = new PanelBuilder(layout);
		builder.addSeparator("Tools", cc.xyw(1, 1, 8));
		int row = ResultsComponentFactory.buildMemoryUsage(model, "Memory usage: ", builder, layout, 3, parent);

		builder.add(createGenerateCodeButton(d_network, SyntaxType.BUGS, model), cc.xy(1, row));
		builder.add(createGenerateCodeButton(d_network, SyntaxType.JAGS, model), cc.xy(3, row));

		return builder.getPanel();
	}

	private JButton createGenerateCodeButton(final Network network, final SyntaxType type, final MTCModelWrapper<?> model) {
		JButton button = new JButton("Generate " + type.toString() + " code");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new GeneratedCodeWindow(type, network, model).setVisible(true);
			}
		});
		return button;
	}
}
