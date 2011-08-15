package org.drugis.mtc.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;

public class DataSetView extends JPanel {
	private static final long serialVersionUID = 2396416693795019960L;

	private final DataSetModel d_model;
	private final JFrame d_parent;

	public DataSetView(JFrame parent, DataSetModel model) {
		d_parent = parent;
		d_model = model;
		setLayout(new BorderLayout());
		initComponents();
	}

	private void initComponents() {
		JComponent entityPane = buildEntityPane();
		JComponent infoPane = buildInfoPane();
		JComponent dataPane = buildDataPane();
		JPanel rightPane = new JPanel(new BorderLayout());
		rightPane.add(infoPane, BorderLayout.NORTH);
		rightPane.add(dataPane, BorderLayout.CENTER);
		JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, entityPane, rightPane);
		mainPane.setDividerLocation(200);
		add(mainPane, BorderLayout.CENTER);
	}
	
	private JComponent buildEntityPane() {
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setMinimumSize(new Dimension(200, 400));
		tabbedPane.setTabPlacement(JTabbedPane.TOP);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		JComponent treatmentPane = new ListEditor<TreatmentModel>(d_model.getTreatments(), new TreatmentActions(d_parent, d_model.getStudies()));
		tabbedPane.addTab("Treatments", null, treatmentPane, "Manage treatments");

		JComponent studyPane = new ListEditor<StudyModel>(d_model.getStudies(), new StudyActions(d_parent, d_model.getTreatments()));
		tabbedPane.addTab("Studies", null, studyPane, "Manage studies");

		return tabbedPane;
	}

	public JComponent buildDataPane() {
		JTable table = new JTable(new MeasurementTableModel(d_model));
		TableCellRenderer numberRenderer = new DefaultTableCellRenderer() {
			private static final long serialVersionUID = -1979169367189416419L;

			@Override
			public void setValue(Object value) {
				if (value == null) {
					setText("");
					setBackground(Color.LIGHT_GRAY);
				} else {
					setText(value.toString());
					setBackground(Color.WHITE);
				}
			}
		};
		table.setDefaultRenderer(Integer.class, numberRenderer);
		table.setDefaultRenderer(Double.class, numberRenderer);
		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			private static final long serialVersionUID = -3402316110673827477L;

			@Override
			public void setValue(Object value) {
				if (value instanceof StudyModel) {
					setText(((StudyModel)value).getId());
					setBackground(Color.LIGHT_GRAY);
				} else if (value instanceof TreatmentModel) {
					setText(((TreatmentModel)value).getId());
					setBackground(Color.WHITE);
				}
			}
		});
		return new JScrollPane(table);
	}

	public JComponent buildInfoPane() {
		JPanel panel = new JPanel(new FlowLayout());
		panel.add(new JLabel("A "));
		SelectionInList<MeasurementType> typeSelect = new SelectionInList<MeasurementType>(MeasurementType.values(), d_model.getMeasurementType());
		panel.add(BasicComponentFactory.createComboBox(typeSelect));
		panel.add(new JLabel(" dataset about "));
		JTextField descriptionField = BasicComponentFactory.createTextField(d_model.getDescription(), false);
		descriptionField.setColumns(20);
		panel.add(descriptionField);
		return panel;
	}
}
