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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.tree.TreePath;

import org.drugis.common.beans.ValueEqualsModel;
import org.drugis.common.gui.LayoutUtil;
import org.drugis.common.gui.TextComponentFactory;
import org.drugis.common.gui.table.EnhancedTable;
import org.drugis.common.gui.table.TablePanel;
import org.drugis.common.validation.BooleanAndModel;
import org.drugis.common.validation.BooleanNotModel;
import org.drugis.mtc.ConsistencyModel;
import org.drugis.mtc.DefaultModelFactory;
import org.drugis.mtc.InconsistencyModel;
import org.drugis.mtc.MCMCSettings;
import org.drugis.mtc.MCMCSettingsCache;
import org.drugis.mtc.ModelFactory;
import org.drugis.mtc.NodeSplitModel;
import org.drugis.mtc.data.DataType;
import org.drugis.mtc.graph.GraphUtil;
import org.drugis.mtc.gui.GeneratedCodeWindow.SyntaxType;
import org.drugis.mtc.gui.results.ConsistencyView;
import org.drugis.mtc.gui.results.InconsistencyView;
import org.drugis.mtc.gui.results.NodeSplitView;
import org.drugis.mtc.gui.results.ResultsComponentFactory;
import org.drugis.mtc.gui.results.SimulationComponentFactory;
import org.drugis.mtc.gui.results.SummaryCellRenderer;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.parameterization.NetworkModel;
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

import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.beans.PropertyConnector;
import com.jgoodies.binding.value.AbstractValueModel;
import com.jgoodies.binding.value.ConverterFactory;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class AnalysisView extends JPanel {
	private static final String WARNING_DATASET_CHANGED = "WARNING: the data set has changed since the models were last generated. Regenerate the models to take the new data into account.";

	private static final long serialVersionUID = -3923180226772918488L;

	private final JFrame d_parent;
	private final DataSetModel d_dataset;
	private final AnalysesModel d_analyses = new AnalysesModel();
	private Network d_network;
	private JSplitPane d_mainPane;

	private ModelFactory d_factory = DefaultModelFactory.instance();

	private ValueHolder d_chains = new ValueHolder(d_factory.getDefaults().getNumberOfChains());
	private ValueHolder d_scale = new ValueHolder(d_factory.getDefaults().getVarianceScalingFactor());
	private ValueHolder d_tuning = new ValueHolder(d_factory.getDefaults().getTuningIterations());
	private ValueHolder d_simulation = new ValueHolder(d_factory.getDefaults().getSimulationIterations());
	private ValueHolder d_thinning = new ValueHolder(d_factory.getDefaults().getThinningInterval());

	private MCMCPresentation d_consistency;

	private ValueEqualsModel d_revisionEqualsModel;
	private ValueModel d_haveAnalysesModel;
	private ValueModel d_dataSetChangedModel;

	public class CompleteModel extends AbstractValueModel {
		private static final long serialVersionUID = 5139716568335240809L;

		private boolean d_value;

		public CompleteModel() {
			PropertyChangeListener listener = new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					update();
				}
			};
			d_chains.addPropertyChangeListener(listener);
			d_scale.addPropertyChangeListener(listener);
			d_tuning.addPropertyChangeListener(listener);
			d_simulation.addPropertyChangeListener(listener);
			d_thinning.addPropertyChangeListener(listener);
			update();
		}

		private void update() {
			boolean oldValue = d_value;
			boolean newValue =
				d_chains.getValue() != null && d_scale.getValue() != null &&
				d_tuning.getValue() != null && d_simulation.getValue() != null &&
				d_thinning.getValue() != null &&
				getInt(d_chains) > 0 && getDouble(d_scale) > 0.0 &&
				getInt(d_tuning) > 0 && getInt(d_tuning) % 100 == 0 &&
				getInt(d_simulation) > 0 && getInt(d_simulation) % 100 == 0 &&
				getInt(d_thinning) > 0;
			d_value = newValue;
			fireValueChange(oldValue, newValue);
		}

		@Override
		public Object getValue() {
			return d_value;
		}

		@Override
		public void setValue(Object newValue) {
			throw new UnsupportedOperationException();
		}
	}

	public AnalysisView(JFrame parent, DataSetModel model) {
		d_parent = parent;
		d_dataset = model;
		final PropertyAdapter<DataSetModel> revision = new PropertyAdapter<DataSetModel>(d_dataset, DataSetModel.PROPERTY_REVISION, true);
		d_revisionEqualsModel = new ValueEqualsModel(
				revision,
				d_dataset.getRevision());
		d_haveAnalysesModel = new ValueHolder(false);
		d_dataSetChangedModel = new BooleanAndModel(new BooleanNotModel(d_revisionEqualsModel), d_haveAnalysesModel);
		setLayout(new BorderLayout());
		initComponents();
	}

	private void initComponents() {
		JTree tree = new JTree(d_analyses);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				setViewForPath(e.getPath());
			}
		});
		tree.setEditable(false);

		JScrollPane treePane = new JScrollPane(tree);
		treePane.setMinimumSize(new Dimension(150, 100));
		JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePane, new JPanel());
		add(mainPane, BorderLayout.CENTER);
		d_mainPane = mainPane;
		setViewForPath(new TreePath(d_analyses.getRoot()));
	}

	protected JPanel buildDataChangedWarningPanel() {
		JPanel panel = AuxComponentFactory.createWarningPanel(WARNING_DATASET_CHANGED);
		PropertyConnector.connectAndUpdate(d_dataSetChangedModel, panel, "visible");
		return panel;
	}

	private void generateModels() {
		try {
			d_network = d_dataset.cloneNetwork();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		d_revisionEqualsModel.setExpected(d_dataset.getRevision());
		d_haveAnalysesModel.setValue(true);

		d_factory.setDefaults(getSettings());
		d_consistency = buildConsistencyModel();
		d_analyses.add(ModelType.Consistency, d_consistency);
		d_analyses.add(ModelType.Inconsistency, buildInconsistencyModel());
		for (BasicParameter node : d_factory.getSplittableNodes(d_network)) {
			d_analyses.add(ModelType.NodeSplit, buildNodeSplitModel(node));
		}
	}

	private MCMCSettings getSettings() {
		return new MCMCSettingsCache(0, getInt(d_simulation), getInt(d_thinning), getInt(d_tuning), getDouble(d_scale), getInt(d_chains));
	}

	private JTextField createDoubleField(ValueHolder model) {
		JTextField field = new JTextField(10);
		field.setHorizontalAlignment(JTextField.RIGHT);
		Bindings.bind(field,
	              ConverterFactory.createStringConverter(model, new DecimalFormat("0.#####")),
	              true);
		return field;
	}

	private JTextField createIntegerField(ValueHolder model) {
		JTextField field = new JTextField(10);
		field.setHorizontalAlignment(JTextField.RIGHT);
		Bindings.bind(field,
	              ConverterFactory.createStringConverter(model, NumberFormat.getIntegerInstance()),
	              true);
		return field;
	}

	private int getInt(ValueModel model) {
		return ((Number)model.getValue()).intValue();
	}

	private double getDouble(ValueModel model) {
		return ((Number)model.getValue()).doubleValue();
	}

	private MCMCPresentation buildConsistencyModel() {
		ConsistencyModel model = d_factory.getConsistencyModel(d_network);
		MCMCModelWrapper wrapper = new SimulationConsistencyWrapper<Treatment>(
				model,
				d_network.getTreatments(),
				Util.identityMap(d_network.getTreatments()));
		MCMCPresentation presentation = new MCMCPresentation(wrapper, "Consistency model");
		return presentation;
	}

	private MCMCPresentation buildInconsistencyModel() {
		InconsistencyModel model = d_factory.getInconsistencyModel(d_network);
		MCMCModelWrapper wrapper = new SimulationInconsistencyWrapper<Treatment>(
				model,
				Util.identityMap(d_network.getTreatments()));
		MCMCPresentation presentation = new MCMCPresentation(wrapper, "Inconsistency model");
		return presentation;
	}

	private MCMCPresentation buildNodeSplitModel(BasicParameter split) {
		NodeSplitModel model = d_factory.getNodeSplitModel(d_network, split);
		MCMCModelWrapper wrapper = new SimulationNodeSplitWrapper<Treatment>(
				model,
				Util.identityMap(d_network.getTreatments()));
		MCMCPresentation presentation = new MCMCPresentation(wrapper, split.getName());
		return presentation;
	}

	private JPanel buildRootPanel() {
		CellConstraints cc = new CellConstraints();
		FormLayout layout = new FormLayout("pref, 3dlu, pref, fill:0:grow",
				"p");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();

		int row = LayoutUtil.addRow(layout, 1);
		builder.add(TextComponentFactory.createTextPane(boxHtmlText(Help.getHelpText("networkMetaAnalysis")), false), cc.xyw(1, row, 4));

		row = LayoutUtil.addRow(layout, row);
		builder.addSeparator("Generate models", cc.xyw(1, 1, layout.getColumnCount()));

		row = LayoutUtil.addRow(layout, row);
		builder.add(new JLabel("Number of chains: "), cc.xy(1, row));
		builder.add(createIntegerField(d_chains), cc.xy(3, row));

		row = LayoutUtil.addRow(layout, row);
		builder.add(new JLabel("Initial values scaling: "), cc.xy(1, row));
		builder.add(createDoubleField(d_scale), cc.xy(3, row));

		row = LayoutUtil.addRow(layout, row);
		builder.add(new JLabel("Tuning iterations: "), cc.xy(1, row));
		builder.add(createIntegerField(d_tuning), cc.xy(3, row));

		row = LayoutUtil.addRow(layout, row);
		builder.add(new JLabel("Simulation iterations: "), cc.xy(1, row));
		builder.add(createIntegerField(d_simulation), cc.xy(3, row));

		row = LayoutUtil.addRow(layout, row);
		builder.add(new JLabel("Thinning interval: "), cc.xy(1, row));
		builder.add(createIntegerField(d_thinning), cc.xy(3, row));

		final JButton button = new JButton("Generate models");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final DataSetModel model = d_dataset;
				if (model.getTreatments().size() < 2 || model.getStudies().size() < 2) {
					JOptionPane.showMessageDialog(d_parent, "You need to define at least two studies and treatments.", "Cannot generate model", JOptionPane.WARNING_MESSAGE);
					return;
				}
				if (model.getMeasurementType().getValue() == DataType.NONE) {
					JOptionPane.showMessageDialog(d_parent, "Model generation not possible with 'None' measuments.", "Cannot generate model", JOptionPane.WARNING_MESSAGE);
					return;
				}
				Network network = model.getNetwork();
				if (!GraphUtil.isWeaklyConnected(NetworkModel.createStudyGraph(network))) {
					JOptionPane.showMessageDialog(d_parent, "The network needs to be connected in order to generate an MTC model.", "Cannot generate model", JOptionPane.WARNING_MESSAGE);
					return;
				}
				// FIXME: further validation / checking of data.
//				final String name = model.getFile() == null ? "unnamed" : model.getFile().getName().replaceFirst(".gemtc$", "");

				d_analyses.clear();
				generateModels();
			}
		});
		PropertyConnector.connectAndUpdate(new CompleteModel(), button, "enabled");
		row = LayoutUtil.addRow(layout, row);
		builder.add(button, cc.xy(3, row));

		return builder.getPanel();
	}

	private JPanel buildTypePanel(ModelType type) {
		CellConstraints cc = new CellConstraints();
		FormLayout layout = new FormLayout("pref, pref:grow:fill", "p, 3dlu, p, 3dlu, p");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		switch (type) {
		case Consistency:
			builder.addSeparator("Consistency model", cc.xyw(1, 1, 2));
			builder.add(TextComponentFactory.createTextPane(boxHtmlText(Help.getHelpText("consistency")), false), cc.xy(1, 3));
			break;
		case Inconsistency:
			builder.addSeparator("Inconsistency model", cc.xy(1, 1));
			builder.add(TextComponentFactory.createTextPane(boxHtmlText(Help.getHelpText("inconsistency")), false), cc.xy(1, 3));
			break;
		case NodeSplit:
			builder.addSeparator("Node splitting models", cc.xy(1, 1));
			builder.add(TextComponentFactory.createTextPane(boxHtmlText(Help.getHelpText("nodeSplit")), false), cc.xy(1, 3));
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
			builder.add(new TablePanel(table), cc.xy(1, 5));
			break;
		default:
			break;
		}

		return builder.getPanel();
	}

	private String boxHtmlText(String text) {
		return "<div style='width: 400px; padding: 0px 10px 10px 10px'>" + text + "</div>";
	}

	private JPanel buildModelPanel(final MCMCPresentation presentation) {
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

		Runnable onReset = new Runnable() {
			public void run() {
				MCMCPresentation newPresentation = null;
				if (presentation.getModel() instanceof ConsistencyModel) {
					newPresentation = buildConsistencyModel();
					d_analyses.replace(ModelType.Consistency, presentation, newPresentation);
				} else if (presentation.getModel() instanceof InconsistencyModel) {
					newPresentation = buildInconsistencyModel();
					d_analyses.replace(ModelType.Inconsistency, presentation, newPresentation);
				} else if (presentation.getModel() instanceof NodeSplitModel) {
					NodeSplitModel nodeSplitModel = (NodeSplitModel) presentation.getModel();
					newPresentation = buildNodeSplitModel(nodeSplitModel.getSplitNode());
					d_analyses.replace(ModelType.NodeSplit, presentation, newPresentation);
				}
				setView(buildModelPanel(newPresentation));
			}
		};

		builder.setDefaultDialogBorder();
		builder.add(SimulationComponentFactory.createSimulationControls(presentation, d_parent, true, null, onReset), cc.xy(1, 1));
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

	private void setViewForPath(TreePath path) {
		JPanel view = null;
		if (path.getLastPathComponent() instanceof MCMCPresentation) {
			view = buildModelPanel((MCMCPresentation)path.getLastPathComponent());
		} else if (path.getLastPathComponent() instanceof ModelType) {
			view = buildTypePanel((ModelType)path.getLastPathComponent());
		} else {
			view = buildRootPanel();
		}
		setView(view);
	}

	private void setView(JPanel view) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(buildDataChangedWarningPanel(), BorderLayout.NORTH);
		panel.add(new JScrollPane(view), BorderLayout.CENTER);
		d_mainPane.setRightComponent(panel);
	}
}
