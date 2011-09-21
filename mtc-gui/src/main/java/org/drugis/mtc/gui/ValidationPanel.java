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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.drugis.common.validation.BooleanAndModel;

import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.beans.PropertyConnector;
import com.jgoodies.binding.value.ConverterFactory;
import com.jgoodies.binding.value.ValueModel;

/**
 * Implements a panel with error messages bound to validators and an OK button that is enabled only if all validations are true. 
 */
public class ValidationPanel extends JPanel {
	private static final long serialVersionUID = -1393268128385357069L;

	public static class Validation {
		public final ValueModel validator;
		public final String errorText;
		
		public Validation(ValueModel validator, String errorText) {
			this.validator = validator;
			this.errorText = errorText;
		}
	}
	
	public ValidationPanel(List<Validation> validators, ActionListener okListener) {
		super(new GridLayout(0, 1, 2, 2));
		
		JPanel errorPanel = new JPanel(new FlowLayout());
		
		List<ValueModel> models = new ArrayList<ValueModel>();
		for (Validation validation : validators) {
			models.add(validation.validator);
			errorPanel.add(buildErrorLabel(validation.validator, validation.errorText));
		}

		JButton okButton = new JButton("OK");
		PropertyConnector.connectAndUpdate(new BooleanAndModel(models), okButton, "enabled");
		okButton.addActionListener(okListener);
		
		add(errorPanel);
		add(okButton);
	}
	
	private JLabel buildErrorLabel(ValueModel validator, String errorText) {
		JLabel notEmptyLabel = new JLabel(errorText);
		notEmptyLabel.setForeground(Color.RED);
		Bindings.bind(notEmptyLabel, "visible", ConverterFactory.createBooleanNegator(validator));
		return notEmptyLabel;
	}
}
