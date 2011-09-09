/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.commons.math.random.JDKRandomGenerator;
import org.drugis.common.ImageLoader;
import org.drugis.common.gui.FileDialog;
import org.drugis.common.gui.FileLoadDialog;
import org.drugis.common.gui.FileSaveDialog;
import org.drugis.mtc.ConsistencyNetworkModel$;
import org.drugis.mtc.RandomizedStartingValueGenerator$;
import org.drugis.mtc.Measurement;
import org.drugis.mtc.Network;
import org.drugis.mtc.NetworkModel;
import org.drugis.mtc.StartingValueGenerator;
import org.drugis.mtc.jags.JagsSyntaxModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.beans.PropertyConnector;
import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.list.ObservableList;
import com.jgoodies.binding.value.AbstractValueModel;
import com.jgoodies.binding.value.ValueModel;

public class MainWindow extends JFrame {
	public class FileNameModel extends AbstractValueModel {
		private static final long serialVersionUID = -8194830838726012699L;

		private ValueModel d_file;
		private String d_value;

		public FileNameModel(DataSetModel dataSet) {
			d_file = new PropertyAdapter<DataSetModel>(dataSet, DataSetModel.PROPERTY_FILE, true);
			d_file.addValueChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent arg0) {
					String oldValue = d_value;
					d_value = calc();
					fireValueChange(oldValue, d_value);
				}
			});
			d_value = calc();
		}

		private String calc() {
			if (d_file.getValue() == null) {
				return "new file";
			} else {
				return ((File)d_file.getValue()).getName();
			}
		}

		public Object getValue() {
			return d_value;
		}

		public void setValue(Object newValue) {
			throw new UnsupportedOperationException();
		}

	}


	private static final long serialVersionUID = -5199299195474870618L;

	public static void main(String[] args) {
		ImageLoader.setImagePath("/org/drugis/mtc/gui/");
		new MainWindow().setVisible(true);
	}

	private JTabbedPane d_mainPane;
	private ObservableList<DataSetModel> d_models = new ArrayListModel<DataSetModel>();

	public MainWindow() {
		super("drugis.org MTC");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		setMinimumSize(new Dimension(750, 550));
		setLayout(new BorderLayout());
		
		d_mainPane = new JTabbedPane();
		add(createToolBar(), BorderLayout.NORTH);
		add(d_mainPane , BorderLayout.CENTER);
	}
	
	private void addModel(DataSetModel model) {
		int index = d_models.size();
		d_models.add(model);
		DataSetView view = new DataSetView(MainWindow.this, model);
		JComponent tabHeader = BasicComponentFactory.createLabel(new FileNameModel(model));
		d_mainPane.add(view);
		d_mainPane.setTabComponentAt(index, tabHeader);
	}
	
	private JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

		toolbar.add(createNewButton());
		toolbar.add(createOpenButton());
		toolbar.add(createSaveButton());
		toolbar.add(createGenerateButton());

        return toolbar;
	}

	private JButton createNewButton() {
		JButton newButton = new JButton("New", ImageLoader.getIcon("newfile.gif"));
		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addModel(new DataSetModel());
			}
		});
		return newButton;
	}

	private JButton createOpenButton() {
		JButton openButton = new JButton("Open", ImageLoader.getIcon("openfile.gif"));
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.err.println("HELLO");
				FileDialog dialog = new FileLoadDialog(MainWindow.this, "xml", "XML files") {
					public void doAction(String path, String extension) {
						final File file = new File(path);
						final DataSetModel model = readFromFile(file);
						model.setFile(file);
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								addModel(model);
							}
						});
					}
				};
				dialog.setVisible(true);
			}
		});
		return openButton;
	}


	private JButton createSaveButton() {
		JButton saveButton = new JButton("Save", ImageLoader.getIcon("savefile.gif"));
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					final DataSetModel model = getActiveModel();
					if (model.getFile() == null) {
						FileDialog dialog = new FileSaveDialog(MainWindow.this, "xml", "XML files") {
							public void doAction(String path, String extension) {
								File file = new File(path);
								writeToFile(model, file);
								model.setFile(file);
							}
						};
						dialog.setVisible(true);
					} else {
						writeToFile(model, model.getFile());
					}
				} catch (IllegalArgumentException e) {
					JOptionPane.showMessageDialog(MainWindow.this, "Error: " + e.getMessage(), "File could not be saved.", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		return saveButton;
	}

	@SuppressWarnings("unchecked")
	private JButton createGenerateButton() {
		JButton button = new JButton("Generate", ImageLoader.getIcon("generate.gif"));
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				final DataSetModel model = getActiveModel();
				if (model.getTreatments().size() < 2 || model.getStudies().size() < 2) {
					JOptionPane.showMessageDialog(MainWindow.this, "You need to define at least two studies and treatments.", "Cannot generate model", JOptionPane.WARNING_MESSAGE);
					return;
				}
				Network network;
				try {
					network = model.build();
				} catch (IllegalArgumentException e) {
					JOptionPane.showMessageDialog(MainWindow.this, "Error: " + e.getMessage(), "Cannot generate model", JOptionPane.WARNING_MESSAGE);
					return;
				}
				final NetworkModel nm = ConsistencyNetworkModel$.MODULE$.apply(network);
				final JagsSyntaxModel jagsSyntaxModel = new JagsSyntaxModel(nm);
				final String name = model.getFile() == null ? "unnamed" : model.getFile().getName().replaceFirst(".xml$", "");
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						showSyntaxModel(nm, jagsSyntaxModel, name);	
					}
				});
				
			}
			
		});
		PropertyConnector.connectAndUpdate(new ListMinimumSizeModel(d_models, 1), button, "enabled");
		return button;
	}

	@SuppressWarnings("unchecked")
	private void showSyntaxModel(NetworkModel nm, JagsSyntaxModel jagsSyntaxModel, String name) {
		JDialog dialog = new JDialog(this, "Jags consistency model: " + name);
		JTabbedPane tabbedPane = new JTabbedPane();
		
		tabbedPane.addTab("Model", new JScrollPane(new JTextArea(jagsSyntaxModel.modelText())));
		tabbedPane.addTab("Data", new JScrollPane(new JTextArea(jagsSyntaxModel.dataText())));
		final int chains = 4;
		tabbedPane.addTab("Script", new JScrollPane(new JTextArea(jagsSyntaxModel.scriptText(name, chains, 20000, 40000))));
		StartingValueGenerator gen = RandomizedStartingValueGenerator$.MODULE$.apply(nm, new JDKRandomGenerator(), 2.5);
		for (int i = 1; i <= 4; ++i) {
			tabbedPane.addTab("Inits " + i, new JScrollPane(new JTextArea(jagsSyntaxModel.initialValuesText(gen))));
		}
		
		dialog.add(tabbedPane);
		dialog.pack();
		dialog.setVisible(true);
	}
	
	private DataSetModel readFromFile(final File file) {
		InputStream is;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		Network<? extends Measurement> network = Network.fromXML(scala.xml.XML.load(is));
		return DataSetModel.build(network);
	}
	
	private void writeToFile(final DataSetModel model, final File file) {
		OutputStreamWriter os;
		try {
			os = new OutputStreamWriter(new FileOutputStream(file));
			os.write(model.build().toPrettyXML());
			os.write("\n");
			os.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private DataSetModel getActiveModel() {
		int idx = d_mainPane.getSelectedIndex();
		if (idx >= 0) {
			return d_models.get(idx);
		} else {
			return null;
		}
	}
}
