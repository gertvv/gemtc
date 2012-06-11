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

package org.drugis.mtc.yadas;

import gov.lanl.yadas.MCMCParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;
import org.drugis.mtc.Parameter;

public class YadasResults implements MCMCResults {
	private Parameter[] d_directParameters;
	private Parameter[] d_derivedParameters;
	private Derivation[] d_derivations;
	private int d_nChains;
	private int d_availableSamples;
	private int d_reservedSamples;
	private List<List<double[]>> d_results;
	private List<MCMCResultsListener> d_listeners = new ArrayList<MCMCResultsListener>();
	
	private class YadasParameterWriter extends ParameterWriter {
		private final int d_pIdx;
		private final int d_cIdx;
		private int d_idx;

		/**
		 * @param pIdx Index of the parameter in the results.
		 * @param cIdx Chain index.
		 * @param mp MCMCParameter to monitor.
		 * @param i The component of the MCMCParameter to monitor.
		 */
		public YadasParameterWriter(int pIdx, int cIdx, MCMCParameter mp, int i) {
			super(mp, i);
			d_pIdx = pIdx;
			d_cIdx = cIdx;
			d_idx = 0;
		}

		@Override
		protected void write(double value) {
			d_results.get(d_cIdx).get(d_pIdx)[d_idx++] = value;
		}
	}
	
	public YadasResults() {
		initialize();
	}

	@Override
	public void clear() {
		d_availableSamples = 0;
		d_reservedSamples = 0;
		initResults();
	}
	
	private void initialize() { 
		d_directParameters = new Parameter[] {};
		d_derivedParameters = new Parameter[] {};
		d_derivations = new Derivation[] {};
		d_nChains = 0;
		clear();
	}

	/**
	 * Set the parameters for which results will be produced by the Yadas simulation.
	 */
	public void setDirectParameters(List<Parameter> parameters) {
		d_directParameters = new Parameter[parameters.size()];
		for (int i = 0; i < parameters.size(); ++i) {
			d_directParameters[i] = parameters.get(i);
		}
		initResults();
	}

	/**
	 * Set the derived parameters, which are to be calculated from the direct ones using the given Derivation.
	 */
	public void setDerivedParameters(Map<? extends Parameter, Derivation> derivations) {
		d_derivedParameters = new Parameter[derivations.size()];
		d_derivations = new Derivation[derivations.size()];
		int i = 0;
		for (Entry<? extends Parameter, Derivation> e : derivations.entrySet()) {
			d_derivedParameters[i] = e.getKey();
			d_derivations[i] = e.getValue();
			++i;
		}
	}

	/**
	 * Get the direct parameters.
	 * @see MCMCResults
	 */
	@Override
	public Parameter[] getParameters() {
		return d_directParameters;
	}

	@Override
	public int findParameter(Parameter p) {
		int idx = ArrayUtils.indexOf(d_directParameters, p);
		if (idx >= 0) {
			return idx;
		}
		idx = ArrayUtils.indexOf(d_derivedParameters, p);
		if (idx >= 0) {
			idx += d_directParameters.length;
		}
		return idx;
	}

	/**
	 * Create the ParameterWriter for the given Parameter.
	 * @param p Model parameter to write results for.
	 * @param cIdx Chain index.
	 * @param mp MCMCParameter to monitor results from.
	 * @param i Component of the MCMCParameter to monitor.
	 * @return A new ParameterWriter.
	 */
	public ParameterWriter getParameterWriter(Parameter p, int cIdx, MCMCParameter mp, int i) {
		return new YadasParameterWriter(findParameter(p), cIdx, mp, i);
	}

	@Override
	public double getSample(int p, int c, int i) {
		if (c < 0 || c >= d_nChains) {
			throw new IndexOutOfBoundsException("Chain " + c + " out of bounds: " +
					d_nChains + " chains available.");
		}
		final int nDirect = d_directParameters.length;
		final int nDerived = d_derivedParameters.length;
		if (p < 0 || p >= nDirect + nDerived) {
			throw new IndexOutOfBoundsException("Parameter " + p + " out of bounds: " + nDirect +
					" + " + nDerived + " parameters available.");
		}
		if (i < 0 || i >= d_availableSamples) {
			throw new IndexOutOfBoundsException("Iteration " + i + " out of bounds: " +
					d_availableSamples + " iterations available.");
		}
		return p < nDirect ? d_results.get(c).get(p)[i] : d_derivations[p - nDirect].calculate(this, c, i);
	}


	public void setNumberOfChains(int nChains) {
		d_nChains = nChains;
		initResults();
	}

	private void initResults() {
		d_results = new ArrayList<List<double[]>>(d_nChains);
		for (int i = 0; i < d_nChains; ++i) {
			List<double[]> list = new ArrayList<double[]>(d_directParameters.length);
			for (int j = 0; j < d_directParameters.length; ++j) {
				list.add(new double[d_reservedSamples]);
			}
			d_results.add(list);
		}
	}

	@Override
	public int getNumberOfChains() {
		return d_nChains;
	}
	
	public void setNumberOfIterations(int iter) {
		d_reservedSamples = iter;
		for (List<double[]> chain : d_results) {
			for (int i = 0; i < chain.size(); ++i) {
				double[] newResults = new double[iter];
				System.arraycopy(chain.get(i), 0, newResults, 0, chain.get(i).length);
				chain.set(i, newResults);
			}
		}
	}
	
	public int getNumberOfIterations() { 
		return d_reservedSamples;
	}

	@Override
	public int getNumberOfSamples() {
		return d_availableSamples;
	}
	
	public void simulationFinished() {
		d_availableSamples = d_reservedSamples;
		MCMCResultsEvent event = new MCMCResultsEvent(this);
		List<MCMCResultsListener> listeners = new ArrayList<MCMCResultsListener>(d_listeners);
		for (MCMCResultsListener l : listeners) {
			l.resultsEvent(event);
		}
	}
	

	
	@Override
	public void addResultsListener(MCMCResultsListener l) {
		d_listeners.add(l);
	}
	
	@Override
	public void removeResultsListener(MCMCResultsListener l) {
		d_listeners.remove(l);
	}
}
