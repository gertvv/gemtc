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

import java.util.List;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.drugis.common.beans.AbstractObservable;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;
import org.drugis.mtc.Parameter;

/**
 * Summary of data assumed to be normally distributed; i.e., that have mean and standard deviation.
 */
public class NormalSummary extends AbstractObservable implements MCMCResultsListener, Summary {
	public static final String PROPERTY_MEAN = "mean";
	public static final String PROPERTY_STANDARD_DEVIATION = "standardDeviation";
	
	private final MCMCResults d_results;
	private final Parameter d_parameter;
	private double d_mean = 0.0;
	private double d_stdev = 0.0;
	private boolean d_defined = false;

	public NormalSummary(MCMCResults results, Parameter parameter) {
		d_results = results;
		d_parameter = parameter;
		calculateResults();
		results.addResultsListener(this);
	}

	public void resultsEvent(MCMCResultsEvent event) {
		calculateResults();
	}

	private boolean isReady() {
		return d_results.getNumberOfSamples() >= 4;
	}
	
	/* (non-Javadoc)
	 * @see org.drugis.mtc.summary.Summary#getDefined()
	 */
	public boolean getDefined() {
		return d_defined;
	}
	
	public double getMean() {
		return d_mean;
	}
		
	public double getStandardDeviation() {
		return d_stdev;
	}
	
	private synchronized void calculateResults() {
		if (!isReady()) return;
		List<Double> samples = SummaryUtil.getAllChainsLastHalfSamples(d_results, d_parameter);
		d_mean = SummaryUtil.evaluate(new Mean(), samples);
		d_stdev = SummaryUtil.evaluate(new StandardDeviation(), samples);	
		d_defined = true;
		firePropertyChange(PROPERTY_DEFINED, null, d_defined);
		firePropertyChange(PROPERTY_MEAN, null, d_mean);
		firePropertyChange(PROPERTY_STANDARD_DEVIATION, null, d_stdev);
	}
}
