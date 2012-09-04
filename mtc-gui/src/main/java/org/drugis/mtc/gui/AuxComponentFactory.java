package org.drugis.mtc.gui;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import org.drugis.common.gui.GUIHelper;

public class AuxComponentFactory {

	public static JPanel createWarningPanel(String warningText) {
		// Get the warning icon from the LookAndFeel, otherwise use a fall back
		Icon icon = UIManager.getIcon("OptionPane.warningIcon");
		if (icon == null) {
			icon = MainWindow.IMAGELOADER.getIcon(FileNames.ICON_WARNING);
		}
		JLabel iconLabel = new JLabel(icon);
	
		// Use a TextPane for auto-wrapping text
		JTextPane textPane = new JTextPane();
		textPane.setText(warningText);
		textPane.setEditable(false);
		textPane.setOpaque(false);
	
		JPanel panel = new JPanel();
		// BoxLayout ensures the components fit the container
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBackground(GUIHelper.COLOR_WARNING);
	
		panel.add(Box.createRigidArea(new Dimension(8, 0))); // padding
		panel.add(iconLabel);
		panel.add(Box.createRigidArea(new Dimension(8, 0))); // padding
		panel.add(textPane);
		panel.add(Box.createRigidArea(new Dimension(8, 0))); // padding
	
		return panel;
	}

}
