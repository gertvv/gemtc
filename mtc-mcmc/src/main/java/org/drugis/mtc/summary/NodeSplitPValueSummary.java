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

package org.drugis.mtc.summary;

import java.util.List;

import org.drugis.common.beans.AbstractObservable;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;
import org.drugis.mtc.Parameter;

public class NodeSplitPValueSummary extends AbstractObservable implements
		Summary {

	public static final String PROPERTY_PVALUE = "pValue";
	private MCMCResults d_results;
	private Parameter d_direct;
	private Parameter d_indirect;
	private boolean d_defined;
	private double d_pvalue;

	public NodeSplitPValueSummary(MCMCResults r, Parameter dir, Parameter indir) {
		d_results = r;
		d_direct = dir;
		d_indirect = indir;
		d_defined = false;
		
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
		calculatePValue();
		d_defined = true;
		firePropertyChange(PROPERTY_PVALUE, null, d_pvalue);
		firePropertyChange(PROPERTY_DEFINED, false, true);	
	}

	private void calculatePValue() {
		int nDirLargerThanIndir = 0;
		List<Double> directSamples = SummaryUtil.getAllChainsLastHalfSamples(d_results, d_direct);
		List<Double> indirectSamples = SummaryUtil.getAllChainsLastHalfSamples(d_results, d_indirect);
		for (int i = 0; i < directSamples.size(); ++i) {
			if (directSamples.get(i) > indirectSamples.get(i)) ++nDirLargerThanIndir;
		}
		double prop = (double) nDirLargerThanIndir / (double) directSamples.size();
		d_pvalue = 2.0 * Math.min(prop, 1.0 - prop);
	}

	public boolean getDefined() {
		return d_defined;
	}
	
	public double getPvalue() {
		return d_pvalue;
	}

}
