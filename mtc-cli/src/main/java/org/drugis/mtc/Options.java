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

public class Options {
	private final String d_xmlFile;
	private final String d_baseName;
	private final ModelType d_modelType;
	private final double d_scale;
	private final int d_tuningIter;
	private final int d_simulationIter;
	private final boolean d_suppress;
	private final boolean d_bugs;

	public Options(String xmlFile, String baseName, ModelType modelType, double scale,
			int tuningIter, int simulationIter, boolean suppress, boolean bugs) {
		d_xmlFile = xmlFile;
		d_baseName = baseName;
		d_modelType = modelType;
		d_scale = scale;
		d_tuningIter = tuningIter;
		d_simulationIter = simulationIter;
		d_suppress = suppress;
		d_bugs = bugs;
	}

	public String getXmlFile() {
		return d_xmlFile;
	}

	public String getBaseName() {
		return d_baseName;
	}

	public ModelType getModelType() {
		return d_modelType;
	}

	public double getScale() {
		return d_scale;
	}

	public int getTuningIterations() {
		return d_tuningIter;
	}

	public int getSimulationIterations() {
		return d_simulationIter;
	}

	public boolean getSuppressOutput() {
		return d_suppress;
	}

	public boolean getBugsOutput() {
		return d_bugs;
	}
}
