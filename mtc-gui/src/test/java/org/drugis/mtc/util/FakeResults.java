/*
 * This file is part of ADDIS (Aggregate Data Drug Information System).
 * ADDIS is distributed from http://drugis.org/.
 * Copyright © 2009 Gert van Valkenhoef, Tommi Tervonen.
 * Copyright © 2010 Gert van Valkenhoef, Tommi Tervonen, Tijs Zwinkels,
 * Maarten Jacobs, Hanno Koeslag, Florin Schimbinschi, Ahmad Kamal, Daniel
 * Reid.
 * Copyright © 2011 Gert van Valkenhoef, Ahmad Kamal, Daniel Reid, Florin
 * Schimbinschi.
 * Copyright © 2012 Gert van Valkenhoef, Daniel Reid, Joël Kuiper, Wouter
 * Reckman.
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

package org.drugis.mtc.util;

import java.util.ArrayList;
import java.util.List;

import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsEvent;
import org.drugis.mtc.MCMCResultsListener;
import org.drugis.mtc.Parameter;

public class FakeResults implements MCMCResults {
	private int d_nChains;
	private int d_nSamples;
	private int d_nParameters;
	private boolean d_resultsAvailable;
	private List<MCMCResultsListener> d_listeners;
	
	public FakeResults(int nChains, int nSamples, int nParameters, boolean resultsAvailable) {
		d_nChains = nChains;
		d_nSamples = nSamples;
		d_nParameters = nParameters;
		d_resultsAvailable = resultsAvailable;
		d_listeners = new ArrayList<MCMCResultsListener>();
	}

	public FakeResults(int nChains, int nSamples, int nParameters) {
		this(nChains, nSamples, nParameters, true);
	}
	
	public void addResultsListener(MCMCResultsListener l) {
		d_listeners.add(l);
	}
	public void removeResultsListener(MCMCResultsListener l) {
		d_listeners.remove(l);
	}
	public int findParameter(Parameter p) { return 0; }
	public int getNumberOfChains() { return d_nChains; }
	public int getNumberOfSamples() { return d_resultsAvailable ? d_nSamples : 0; }
	public Parameter[] getParameters() { return new Parameter[d_nParameters]; }
	public double getSample(int p, int c, int i) { return 0; }
	public double[] getSamples(int p, int c) { return null; }
	public void clear() {
		d_resultsAvailable = false;
		fireResultsChanged();
	}


	public void makeResultsAvailable() {
		d_resultsAvailable = true;
		fireResultsChanged();
	}
	

	private void fireResultsChanged() {
		for (MCMCResultsListener l : d_listeners) {
			l.resultsEvent(new MCMCResultsEvent(this));
		}
	}
}