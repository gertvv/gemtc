/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2010 Gert van Valkenhoef.
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

/**
 * A MixedTreatmentComparison estimates the relative effects of a set of
 * treatments given an evidence network. The estimates are only provided after
 * the model has been run (using Runnable.run()). It is possible to track the
 * progress of the model run using a ProgressListener.
 */
public interface MixedTreatmentComparison extends Runnable {
	/**
	 * Get the estimated relative effect.
	 * @return The effect estimate.
	 * @param base The treatment to use as baseline.
	 * @param subj The treatment to use as alternative.
	 * @throws IllegalArgumentException if one of the treatments is not
	 * present in the evidence network.
	 * @throws IllegalStateException if the MTC is not ready.
	 */
	public Estimate getRelativeEffect(Treatment base, Treatment subj);
	/**
	 * Add a progress listener to this MTC.
	 */
	public void addProgressListener(ProgressListener l);
	/**
	 * @return false if it's necessary to run() this model before calling any
	 * getters.
	 */
	public boolean isReady();
	/**
	 * @return the number of burn-in iterations
	 */
	public int getBurnInIterations();
	/**
	 * @param it The number of burn-in iterations, a multiple of 100.
	 * @throws IllegalArgumentException if it is not a multiple of 100, or
	 * if it <= 0.
	 */
	public void setBurnInIterations(int it);
	/**
	 * @return the number of simulation iterations
	 */
	public int getSimulationIterations();
	/**
	 * @param it The number of simulation iterations, a multiple of 100.
	 * @throws IllegalArgumentException if it is not a multiple of 100, or
	 * if it <= 0.
	 */
	public void setSimulationIterations(int it);
}
