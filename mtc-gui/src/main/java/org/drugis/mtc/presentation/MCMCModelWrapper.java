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

package org.drugis.mtc.presentation;

import org.drugis.mtc.MCMCModel;
import org.drugis.mtc.MCMCSettings;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.summary.ConvergenceSummary;
import org.drugis.mtc.summary.QuantileSummary;

import com.jgoodies.binding.beans.Observable;

public interface MCMCModelWrapper extends Observable {

	public static final String PROPERTY_DESTROYED = "destroyed";

	/**
	 * Get a human-readable description of the analysis type.
	 */
	public abstract String getDescription();

	/**
	 * Whether or not the model has saved results (rather than newly-computed ones)
	 */
	public abstract boolean isSaved();

	/**
	 * Whether or not the user accepted the results/convergence. If so, the results can be saved.
	 */
	public abstract boolean isApproved();

	/** 
	 * Whether or not the model should be cleaned up on the next invocation from analysis.
	 * This will cause it to create a new instance of a AbstractSimulationModel.
	 */
	public abstract void selfDestruct();

	/**
	 * Returns true if selfDestruct called previously, false otherwise.
	 */
	public abstract boolean getDestroyed();

	/**
	 * @see org.drugis.mtc.MCMCResults#getParameters()
	 */
	public abstract Parameter[] getParameters();

	/**
	 * @see org.drugis.mtc.MixedTreatmentComparison#getSettings()
	 */
	public abstract MCMCSettings getSettings();

	/**
	 * Get a convergence summary for the given parameter.
	 * The parameter must occur in the list returned by {@link #getParameters()}.
	 */
	public abstract ConvergenceSummary getConvergenceSummary(Parameter p);

	/**
	 * Get a quantile summary for the given parameter.
	 * The parameter must occur in the list returned by {@link #getParameters()}, 
	 * or be a relative effect from {@link #getRelativeEffect(TreatmentDefinition, TreatmentDefinition)}.
	 */
	public abstract QuantileSummary getQuantileSummary(Parameter ip);

	/**
	 * Get the underlying MCMC model.
	 * Can not be called when {@link #isSaved()} is true.
	 */
	public abstract MCMCModel getModel();

}