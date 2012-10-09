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
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.drugis.common.ImageLoader;
import org.drugis.common.gui.FileLoadDialog;
import org.drugis.common.gui.FileSaveDialog;
import org.drugis.common.gui.GUIHelper;
import org.drugis.common.gui.LookAndFeel;
import org.drugis.mtc.model.JAXBHandler;
import org.drugis.mtc.model.Network;

import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.value.AbstractValueModel;
import com.jgoodies.binding.value.ValueModel;

public class MainWindow extends JFrame {
	public class FileNameModel extends AbstractValueModel {
		private static final long serialVersionUID = -8194830838726012699L;

		private ValueModel d_file;
		private String d_value;

		public FileNameModel() {
			final PropertyChangeListener listener = new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent arg0) {
					String oldValue = d_value;
					d_value = calc();
					fireValueChange(oldValue, d_value);
				}
			};
			MainWindow.this.addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName().equals(MainWindow.PROPERTY_MODEL)) {
						String oldValue = d_value;
						attachFileListener(listener);
						d_value = calc();
						fireValueChange(oldValue, d_value);
					}
				}
			});
			attachFileListener(listener);
			d_value = calc();
		}

		private String calc() {
			if (d_file == null) {
				return null;
			} else if (d_file.getValue() == null) {
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

		private void attachFileListener(final PropertyChangeListener listener) {
			if (d_file != null) {
				d_file.removeValueChangeListener(listener);
			}
			if (d_model != null) {
				d_file = new PropertyAdapter<DataSetModel>(d_model, DataSetModel.PROPERTY_FILE, true);
				d_file.addValueChangeListener(listener);
			}
		}
	}

	public static final ImageLoader IMAGELOADER = new ImageLoader("/org/drugis/mtc/gui/");
	private static final long serialVersionUID = -5199299195474870618L;

	public static final String PROPERTY_MODEL = "model";
	private static final String BUG_REPORTING_TEXT = "This is probably a bug in GeMTC. Please help us improve GeMTC by reporting this bug to us.";

	public static void main(final String[] args) {
		Runnable main = new Runnable() {
			public void run() {
				GUIHelper.initializeLookAndFeel();
				LookAndFeel.configureJFreeChartLookAndFeel();
				MainWindow main = new MainWindow();
				main.setVisible(true);

				if (args.length > 0) {
					try {
						main.addModel(loadModel(args[0]));
					} catch (Exception e) {

					}
				}

				// Window disposal debug
				System.out.println(System.currentTimeMillis() + " Started...");
				Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
					@Override
					public void run() {
						System.out.println(System.currentTimeMillis() + " Stopped!");
					}
				}));
			}
		};
		GUIHelper.startApplicationWithErrorHandler(main, BUG_REPORTING_TEXT);
	}

	private DataSetModel d_model = null;
	private FileNameModel d_fileNameModel;

	public MainWindow() {
		super();
		createMainWindow();
	}

	public MainWindow(final Network network) {
		this();
		final DataSetModel model = new DataSetModel(network);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				addModel(model);
			}
		});
	}

	private void createMainWindow() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationByPlatform(true);

		setAppIcon(this);

		setMinimumSize(new Dimension(750, 550));
		setLayout(new BorderLayout());

		add(createToolBar(), BorderLayout.NORTH);

		d_fileNameModel = new FileNameModel();
		d_fileNameModel.addValueChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				updateTitle();
			}
		});
		updateTitle();
	}

	private void updateTitle() {
		String title = AppInfo.getAppName() + " " + AppInfo.getAppVersion();
		if (d_fileNameModel.getValue() != null) {
			title += " - " + d_fileNameModel.getValue();
		}
		setTitle(title);
	}

	public static void setAppIcon(JFrame frame) {
		Image image = null;
		try {
			image = ((ImageIcon)MainWindow.IMAGELOADER.getIcon(FileNames.ICON_GEMTC)).getImage();
		} catch (Exception e) {
			// suppress
		}
		if (image != null) {
			frame.setIconImage(image);
		}
	}

	private void addModel(DataSetModel model) {
		if (d_model == null) {
			d_model = model;
			firePropertyChange(PROPERTY_MODEL, null, d_model);

			DataSetView dataView = new DataSetView(MainWindow.this, model);
			JComponent analysisView = new AnalysisView(MainWindow.this, model);

			JTabbedPane pane = new JTabbedPane();
			pane.addTab("Data", dataView);
			pane.addTab("Analysis", analysisView);
			add(pane, BorderLayout.CENTER);
			pack();
		} else {
			MainWindow window = new MainWindow();
			window.addModel(model);
			window.setVisible(true);
		}
	}

	private JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

		toolbar.add(createNewButton());
		toolbar.add(createOpenButton());
		toolbar.add(createSaveButton());
		toolbar.addSeparator();
		toolbar.add(createAboutButton());

        return toolbar;
	}

	private JButton createNewButton() {
		JButton newButton = new JButton("New", MainWindow.IMAGELOADER.getIcon(FileNames.ICON_NEW));
		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addModel(new DataSetModel());
			}
		});
		return newButton;
	}

	private JButton createOpenButton() {
		JButton openButton = new JButton("Open", MainWindow.IMAGELOADER.getIcon(FileNames.ICON_OPEN));
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileLoadDialog dialog = new FileLoadDialog(MainWindow.this, "gemtc", "GeMTC files") {
					public void doAction(final String path, final String extension) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								addModel(loadModel(path));
							}
						});
					}
				};
				dialog.loadActions();
			}
		});
		return openButton;
	}

	private JButton createSaveButton() {
		JButton saveButton = new JButton("Save", MainWindow.IMAGELOADER.getIcon(org.drugis.mtc.gui.FileNames.ICON_SAVEFILE));
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					final DataSetModel model = getModel();
					if (model.getFile() == null) {
						FileSaveDialog dialog = new FileSaveDialog(MainWindow.this, "gemtc", "GeMTC files") {
							public void doAction(String path, String extension) {
								File file = new File(path);
								writeToFile(model, file);
								model.setFile(file);
							}
						};
						dialog.saveActions();
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

	private JButton createAboutButton() {
		JButton aboutButton = new JButton("About", MainWindow.IMAGELOADER.getIcon(FileNames.ICON_ABOUT));
		aboutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				(new AboutDialog(MainWindow.this)).setVisible(true);
			}
		});
		return aboutButton;
	}

	private static DataSetModel readFromFile(final File file) {
		try {
			InputStream is = new FileInputStream(file);
			return readFromStream(is);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static DataSetModel readFromStream(final InputStream is) {
		try {
			Network network = JAXBHandler.readNetwork(is);
			is.close();
			return new DataSetModel(network);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void writeToFile(final DataSetModel model, final File file) {
		try {
			OutputStream os = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(os);
			JAXBHandler.writeNetwork(model.getNetwork(), bos);
			bos.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public DataSetModel getModel() {
		return d_model;
	}

	private static DataSetModel loadModel(String path) {
		final File file = new File(path);
		final DataSetModel model = readFromFile(file);
		model.setFile(file);
		return model;
	}
}
