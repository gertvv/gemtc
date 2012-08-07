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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.drugis.mtc.ConsistencyModel;
import org.drugis.mtc.DefaultModelFactory;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.parameterization.NodeSplitParameterization;
import org.drugis.mtc.presentation.MCMCModelWrapper;
import org.drugis.mtc.presentation.MCMCPresentation;
import org.drugis.mtc.presentation.SimulationConsistencyWrapper;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.adapter.RadioButtonAdapter;
import com.jgoodies.binding.beans.PropertyConnector;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.AbstractValueModel;
import com.jgoodies.binding.value.ConverterFactory;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;

public class CodeGenerationDialog extends JDialog {
	public enum SyntaxType {
		BUGS,
		JAGS,
		YADAS
	}
	public enum ModelType {
		Consistency,
		Inconsistency,
		NodeSplit
	}
	private static final long serialVersionUID = 7179311466635454391L;
	
	private final String d_name;
	private final Network d_network;
	private ValueHolder d_syntaxType = new ValueHolder(SyntaxType.BUGS);
	private ValueHolder d_modelType = new ValueHolder(ModelType.Consistency);
	private ValueHolder d_splitNode = new ValueHolder();

	private ValueHolder d_chains = new ValueHolder(4L);
	private ValueHolder d_scale = new ValueHolder(2.5);
	private ValueHolder d_tuning = new ValueHolder(20000L);
	private ValueHolder d_simulation = new ValueHolder(40000L);

	private JFrame d_parent;
	
	public static class NodeSplitSelectedModel extends AbstractValueModel {
		private static final long serialVersionUID = 4356976776557424644L;

		private ValueHolder d_holder;
		private boolean d_value;

		public NodeSplitSelectedModel(ValueHolder holder) {
			d_holder = holder;
			d_holder.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					update();
				}
			});
			update();
		}

		private void update() {
			boolean oldValue = d_value;
			boolean newValue = ModelType.NodeSplit.equals(d_holder.getValue());
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
	
	public class CompleteModel extends AbstractValueModel {
		private static final long serialVersionUID = 5139716568335240809L;

		private boolean d_value;
		
		public CompleteModel() {
			PropertyChangeListener listener = new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					update();
				}
			};
			d_modelType.addPropertyChangeListener(listener);
			d_splitNode.addPropertyChangeListener(listener);
			d_chains.addPropertyChangeListener(listener);
			d_scale.addPropertyChangeListener(listener);
			d_tuning.addPropertyChangeListener(listener);
			d_simulation.addPropertyChangeListener(listener);
			update();
		}
		
		private void update() {
			boolean oldValue = d_value;
			boolean newValue = 
				d_chains.getValue() != null && d_scale.getValue() != null && 
				d_tuning.getValue() != null && d_simulation.getValue() != null &&
				getInt(d_chains) > 0 && getDouble(d_scale) > 0.0 &&
				getInt(d_tuning) > 0 && getInt(d_tuning) % 100 == 0 &&
				getInt(d_simulation) > 0 && getInt(d_simulation) % 100 == 0;
			if (newValue && ModelType.NodeSplit.equals(d_modelType.getValue())) {
				newValue = newValue && (d_splitNode.getValue() != null);
			}
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

	public CodeGenerationDialog(JFrame parent, String name, Network network) {
		super(parent, "Generate BUGS/JAGS code for " + name, true);
		d_parent = parent;
		d_name = name;
		d_network = network;
		
		setLocationByPlatform(true);
		initComponents();
		pack();
	}
	
	private void initComponents() {
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		
		Insets insets = new Insets(3, 3, 3, 3); 
		GridBagConstraints leftC = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 
				GridBagConstraints.LINE_END, GridBagConstraints.NONE, insets, 0, 0);
		GridBagConstraints rightC = new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, 
				GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets, 0, 0);
		
		add(new JLabel("Syntax: "), leftC);
		JPanel syntaxPanel = new JPanel(new FlowLayout());
		for (SyntaxType type : SyntaxType.values()) {
			syntaxPanel.add(createRadioButton(d_syntaxType, type));
		}
		add(syntaxPanel, rightC);
		
		leftC.gridy++;
		rightC.gridy++;
		
		add(new JLabel("Model type: "), leftC);
		JPanel modelPanel = new JPanel(new FlowLayout());
		modelPanel.add(createRadioButton(d_modelType, ModelType.Consistency));
		modelPanel.add(createRadioButton(d_modelType, ModelType.Inconsistency));
		modelPanel.add(createRadioButton(d_modelType, ModelType.NodeSplit));
		add(modelPanel, rightC);
		
		leftC.gridy++;
		rightC.gridy++;

		
		add(new JLabel("Split node: "), leftC);
		SelectionInList<BasicParameter> splitNodeSelect = new SelectionInList<BasicParameter>(NodeSplitParameterization.getSplittableNodes(d_network), d_splitNode);
		JComboBox splitNode = BasicComponentFactory.createComboBox(splitNodeSelect);
		add(splitNode, rightC);
		PropertyConnector.connectAndUpdate(new NodeSplitSelectedModel(d_modelType), splitNode, "enabled");
		
		leftC.gridy++;
		rightC.gridy++;
		
		add(new JLabel("Number of chains: "), leftC);
		add(createIntegerField(d_chains), rightC);
		
		leftC.gridy++;
		rightC.gridy++;
		
		add(new JLabel("Initial values scaling: "), leftC);
		add(createDoubleField(d_scale), rightC);
		
		leftC.gridy++;
		rightC.gridy++;
		
		add(new JLabel("Tuning iterations: "), leftC);
		add(createIntegerField(d_tuning), rightC);
		
		leftC.gridy++;
		rightC.gridy++;
		
		add(new JLabel("Simulation iterations: "), leftC);
		add(createIntegerField(d_simulation), rightC);
		
		rightC.gridy++;
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(createCancelButton());
		buttonPanel.add(createOkButton());
		add(buttonPanel, rightC);
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

	private JRadioButton createRadioButton(ValueHolder valueModel, Enum<?> choice, boolean enabled) {
		JRadioButton button = new JRadioButton(choice.name());
		button.setModel(new RadioButtonAdapter(valueModel, choice));
		button.setEnabled(enabled);
		return button;		
	}
	
	private JRadioButton createRadioButton(ValueHolder valueModel, Enum<?> choice) {
		return createRadioButton(valueModel, choice, true);
	}


	private JButton createCancelButton() {
		JButton button = new JButton("Cancel");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				dispose();
			}
		});
		return button;
	}

	private JButton createOkButton() {
		JButton button = new JButton("OK");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
				generate();
			}
		});
		PropertyConnector.connectAndUpdate(new CompleteModel(), button, "enabled");
		return button;
	}

	private void generate() {
		if (d_syntaxType.getValue() == SyntaxType.YADAS) {
			JDialog jDialog = new JDialog(d_parent);
			ConsistencyModel model = DefaultModelFactory.instance().getConsistencyModel(d_network);
			BidiMap<Treatment, Treatment> map = new DualHashBidiMap<Treatment, Treatment>();
			for (Treatment t : d_network.getTreatments()) {
				map.put(t, t);
			}
			MCMCModelWrapper wrapper = new SimulationConsistencyWrapper<Treatment>(model, d_network.getTreatments(), map);
			MCMCPresentation presentation = new MCMCPresentation(wrapper, "Consistency model -- WORK IN PROGRESS");
			jDialog.setLocationByPlatform(true);
			jDialog.add(SimulationComponentFactory.createSimulationControls(presentation, d_parent, false, null, null));
			jDialog.pack();
			jDialog.setVisible(true);
		} else {
			JFrame window = new GeneratedCodeWindow(d_name, d_network, 
					(SyntaxType)d_syntaxType.getValue(), (ModelType)d_modelType.getValue(), (BasicParameter)d_splitNode.getValue(),
					getInt(d_chains), getInt(d_tuning), getInt(d_simulation), getDouble(d_scale));
			window.setVisible(true);
		}
	}

	private int getInt(ValueModel model) {
		return ((Number)model.getValue()).intValue();
	}
	
	private double getDouble(ValueModel model) {
		return ((Number)model.getValue()).doubleValue();
	}
}
