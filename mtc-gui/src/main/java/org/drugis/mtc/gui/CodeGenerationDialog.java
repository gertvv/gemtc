package org.drugis.mtc.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.math.random.JDKRandomGenerator;
import org.drugis.mtc.ConsistencyNetworkModel$;
import org.drugis.mtc.InconsistencyNetworkModel$;
import org.drugis.mtc.Network;
import org.drugis.mtc.NetworkModel;
import org.drugis.mtc.RandomizedStartingValueGenerator$;
import org.drugis.mtc.StartingValueGenerator;
import org.drugis.mtc.jags.JagsSyntaxModel;

import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.adapter.RadioButtonAdapter;
import com.jgoodies.binding.value.ConverterFactory;
import com.jgoodies.binding.value.ValueHolder;

public class CodeGenerationDialog extends JDialog {
	private enum SyntaxType {
		BUGS,
		JAGS
	}
	private enum ModelType {
		Consistency,
		Inconsistency,
		NodeSplit
	}
	private static final long serialVersionUID = 7179311466635454391L;
	
	private final String d_name;
	private final Network<?> d_network;
	private ValueHolder d_syntaxType = new ValueHolder(SyntaxType.BUGS);
	private ValueHolder d_modelType = new ValueHolder(ModelType.Consistency);

	private ValueHolder d_chains = new ValueHolder(4L);
	private ValueHolder d_scale = new ValueHolder(2.5);
	private ValueHolder d_tuning = new ValueHolder(20000L);
	private ValueHolder d_simulation = new ValueHolder(40000L);

	public CodeGenerationDialog(JFrame parent, String name, Network<?> network) {
		super(parent, "Generate BUGS/JAGS code for " + name, true);
		d_name = name;
		d_network = network;
		
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
		syntaxPanel.add(createRadioButton(d_syntaxType, SyntaxType.BUGS));
		syntaxPanel.add(createRadioButton(d_syntaxType, SyntaxType.JAGS));
		add(syntaxPanel, rightC);
		
		leftC.gridy++;
		rightC.gridy++;
		
		add(new JLabel("Model type: "), leftC);
		JPanel modelPanel = new JPanel(new FlowLayout());
		modelPanel.add(createRadioButton(d_modelType, ModelType.Consistency));
		modelPanel.add(createRadioButton(d_modelType, ModelType.Inconsistency));
		modelPanel.add(createRadioButton(d_modelType, ModelType.NodeSplit, false));
		add(modelPanel, rightC);
		
		leftC.gridy++;
		rightC.gridy++;
		
		JLabel label = new JLabel("Sorry, node-split models are not available yet");
		label.setForeground(Color.RED);
		add(label, rightC);
		
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
		return button;
	}

	@SuppressWarnings("unchecked")
	public void generate() {
		final NetworkModel nm = buildNetworkModel();
		final JagsSyntaxModel jagsSyntaxModel = new JagsSyntaxModel(nm, d_syntaxType.getValue() == SyntaxType.JAGS);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				showSyntaxModel(nm, jagsSyntaxModel, d_name);	
			}
		});
	}

	private NetworkModel<?, ?> buildNetworkModel() {
		if (d_modelType.getValue() == ModelType.Consistency) {
			return ConsistencyNetworkModel$.MODULE$.apply(d_network);
		} else {
			return InconsistencyNetworkModel$.MODULE$.apply(d_network);
		}
	}

	@SuppressWarnings("unchecked")
	private void showSyntaxModel(NetworkModel nm, JagsSyntaxModel jagsSyntaxModel, String name) {
		JDialog dialog = new JDialog(this, d_syntaxType.getValue().toString() + " consistency model: " + name);
		JTabbedPane tabbedPane = new JTabbedPane();
		
		tabbedPane.addTab("Model", new JScrollPane(new JTextArea(jagsSyntaxModel.modelText())));
		tabbedPane.addTab("Data", new JScrollPane(new JTextArea(jagsSyntaxModel.dataText())));
		final int chains = getInt(d_chains);
		StartingValueGenerator gen = RandomizedStartingValueGenerator$.MODULE$.apply(nm, new JDKRandomGenerator(), (Double)d_scale.getValue());
		for (int i = 1; i <= chains; ++i) {
			tabbedPane.addTab("Inits " + i, new JScrollPane(new JTextArea(jagsSyntaxModel.initialValuesText(gen))));
		}
		tabbedPane.addTab("Script", new JScrollPane(new JTextArea(jagsSyntaxModel.scriptText(name, chains, getInt(d_tuning), getInt(d_simulation)))));

		dialog.add(tabbedPane);
		dialog.pack();
		dialog.setVisible(true);
	}

	private int getInt(ValueHolder model) {
		return ((Long)model.getValue()).intValue();
	}
}
