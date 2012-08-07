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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.drugis.common.gui.LayoutUtil;
import org.drugis.mtc.MCMCSettings;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.presentation.ConvergenceDiagnosticTableModel;
import org.drugis.mtc.presentation.MCMCModelWrapper;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ConvergenceSummaryDialog extends JDialog  {

	private static final String CONVERGENCE_TEXT = "<p>Convergence is assessed using the Brooks-Gelman-Rubin method. " +
			"This method compares within-chain and between-chain variance to calculate the <em>Potential Scale Reduction Factor</em> " +
			"(PSRF). A PSRF close to one indicates approximate convergence has been reached. See S.P. Brooks and A. Gelman (1998), " +
			"<em>General methods for monitoring convergence of iterative simulations</em>, Journal of Computational and Graphical " +
			"Statistics, 7(4): 434-455. <a href=\"http://www.jstor.org/stable/1390675\">JSTOR 1390675</a>." +
			"</p><p>Double click a parameter in the table below to see the convergence plots.</p>";
	
	private static final long serialVersionUID = -220027860371330394L;
	private final JFrame d_mainWindow;
	private final MCMCModelWrapper d_wrapper;
	private final ValueModel d_modelConstructed;
	private ConvergenceDiagnosticTableModel d_tableModel;
	private MCMCSettings d_settings ;

	private JPanel d_settingsPanel;

	private Color d_noteColor;
	
	public ConvergenceSummaryDialog(final JFrame main, final MCMCModelWrapper wrapper, final ValueModel valueModel, String name, Color noteColor) {
		super(main, name);
		setLocationByPlatform(true);
		
		d_mainWindow = main;
		d_wrapper = wrapper;
		d_modelConstructed = valueModel;
		d_settings = d_wrapper.getSettings();
		
		d_noteColor = noteColor;

		d_tableModel = convergenceTable();
		final JPanel panel = createPanel();
		add(new JScrollPane(panel));
		setMinimumSize(new Dimension(500, 250));
		pack();
	}
	
	private JPanel createPanel() { 
		final FormLayout layout = new FormLayout(
				"pref, 3dlu, fill:0:grow",
				"p, 3dlu, p");
		final PanelBuilder builder = new PanelBuilder(layout, new JPanel());
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();

		final JLabel label = new JLabel("<html><div style='margin:0; padding: 10px; width: 450px;'>" + CONVERGENCE_TEXT + "</html>");
		if (d_noteColor != null) {
			label.setOpaque(true);
			label.setBackground(d_noteColor);
		}
		label.setBorder(BorderFactory.createEtchedBorder());
		builder.add(label, cc.xyw(1, 1, 3));
		
		builder.add(buildConvergenceTable(), cc.xy(1, 3));
		d_settingsPanel = buildMCMCSettingsPanel();
		builder.add(d_settingsPanel, cc.xy(3, 3));
		
		final JPanel panel = builder.getPanel();

		d_tableModel.addTableModelListener(new TableModelListener() {
			
			@Override
			public void tableChanged(TableModelEvent e) {
				panel.validate();
			}
		});
		return panel;
	}
	
	private JPanel buildMCMCSettingsPanel() {
		final FormLayout layout = new FormLayout(
				"pref, 7dlu, fill:0:grow",
				"pref");
		int rows = 1;
		final PanelBuilder builder = new PanelBuilder(layout, new JPanel());
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();

		PresentationModel<MCMCSettings> pm = new PresentationModel<MCMCSettings>(d_settings);
		rows = buildSettingsRow(layout, rows, builder, cc, "Number of chains", pm.getModel(MCMCSettings.PROPERTY_NUMBER_OF_CHAINS));
		rows = buildSettingsRow(layout, rows, builder, cc, "Tuning iterations",  pm.getModel(MCMCSettings.PROPERTY_TUNING_ITERATIONS));
		rows = buildSettingsRow(layout, rows, builder, cc, "Simulation iterations", pm.getModel(MCMCSettings.PROPERTY_SIMULATION_ITERATIONS));
		rows = buildSettingsRow(layout, rows, builder, cc, "Thinning interval", pm.getModel(MCMCSettings.PROPERTY_THINNING_INTERVAL));
		rows = buildSettingsRow(layout, rows, builder, cc, "Inference samples", pm.getModel(MCMCSettings.PROPERTY_INFERENCE_SAMPLES));
		rows = buildSettingsRow(layout, rows, builder, cc, "Variance scaling factor",  pm.getModel(MCMCSettings.PROPERTY_VARIANCE_SCALING_FACTOR));
		
		return builder.getPanel();
	}
	
	private int buildSettingsRow(final FormLayout layout, int rows, final PanelBuilder builder, CellConstraints cc,
			String label, ValueModel model) {
		rows = LayoutUtil.addRow(layout, rows);
		builder.add(new JLabel(label), cc.xy(1, rows));
		builder.add(new JLabel(":"), cc.xy(2, rows));
		builder.add(BasicComponentFactory.createLabel(model, new DecimalFormat()), cc.xy(3, rows));
		return rows;
	}

	private JComponent buildConvergenceTable() {
		ConvergenceDiagnosticTableModel tableModel = convergenceTable();
		final JTable convergenceTable = new JTable(tableModel);
		convergenceTable.getColumnModel().getColumn(0).setPreferredWidth(200);

		convergenceTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					JTable table = (JTable)e.getComponent();
					int row = table.convertRowIndexToModel(table.rowAtPoint(e.getPoint()));
					Parameter[] parameters = d_wrapper.getParameters();
					if (row <= parameters.length) {
						Parameter p = parameters[row];
						showConvergencePlots(d_wrapper, p);
					}
				}
			}
		});
		
		JPanel jPanel = new JPanel(new BorderLayout());
		jPanel.add(convergenceTable, BorderLayout.CENTER);
		jPanel.add(convergenceTable.getTableHeader(), BorderLayout.NORTH);
		return jPanel;
	}

	private ConvergenceDiagnosticTableModel convergenceTable() {
		return (d_tableModel == null) ? new ConvergenceDiagnosticTableModel(d_wrapper, d_modelConstructed) : d_tableModel;
	}

	private void showConvergencePlots(MCMCModelWrapper wrapper, Parameter p) {
		if (!wrapper.isSaved() && wrapper.getModel().getResults().getNumberOfSamples() > 0) {
			JDialog dialog = new ConvergencePlotsDialog(d_mainWindow, d_settings, wrapper.getModel(), p);
			dialog.setPreferredSize(new Dimension(d_mainWindow.getWidth() / 5 * 4, d_mainWindow.getHeight() / 5 * 4));
			dialog.setMinimumSize(new Dimension(d_mainWindow.getMinimumSize().width - 100, d_mainWindow.getMinimumSize().height - 100));
			dialog.setModal(true);
			dialog.setLocationRelativeTo(d_mainWindow);
			dialog.setLocationByPlatform(true);
			dialog.pack();
			dialog.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(d_mainWindow, "Convergence plots cannot be shown because the results of " +
					"this analysis has been discarded to save memory.", "No results available", JOptionPane.WARNING_MESSAGE);
		}
	}
}
