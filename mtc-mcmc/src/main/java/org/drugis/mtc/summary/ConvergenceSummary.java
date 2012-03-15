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

package org.drugis.mtc.summary;

import org.drugis.common.beans.AbstractObservable;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.convergence.GelmanRubinConvergence;

public class ConvergenceSummary extends AbstractObservable implements Summary {
	public static final String PROPERTY_PSRF = "scaleReduction";
	private final MCMCResults d_results;
	private final Parameter d_parameter;
	private double d_convergence;
	private boolean d_defined = false;

	public ConvergenceSummary(MCMCResults results, Parameter parameter) {
		d_results = results;
		d_parameter = parameter;
		d_results.addResultsListener(new MCMCResultsListener() {
			public void resultsEvent(MCMCResultsEvent event) {
				calc();
			}
		});
		if (d_results.getNumberOfSamples() > 0) {
			calc();
		}
	}
	
	private void calc() {
		d_convergence = GelmanRubinConvergence.diagnose(d_results, d_parameter);
		d_defined = true;
		firePropertyChange(PROPERTY_PSRF, null, d_convergence);
		firePropertyChange(PROPERTY_DEFINED, false, true);
	}
	
	public boolean getDefined() {
		return d_defined;
	}

	public double getScaleReduction() {
		return d_convergence;
	}
}
