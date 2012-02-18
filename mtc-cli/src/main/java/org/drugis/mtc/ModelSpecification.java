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

package org.drugis.mtc;

import org.drugis.mtc.jags.JagsSyntaxModel;
import org.drugis.mtc.parameterization.StartingValueGenerator;

public class ModelSpecification {
	private final JagsSyntaxModel d_model;
	private final StartingValueGenerator d_generator;
	private final String d_nameSuffix;

	public ModelSpecification(JagsSyntaxModel model, StartingValueGenerator generator, String nameSuffix) {
		d_model = model;
		d_generator = generator;
		d_nameSuffix = nameSuffix;
	}

	public JagsSyntaxModel getModel() {
		return d_model;
	}

	public StartingValueGenerator getGenerator() {
		return d_generator;
	}

	public String getNameSuffix() {
		return d_nameSuffix;
	}
}
