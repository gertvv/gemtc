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

import org.drugis.common.threading.activity.ActivityTask;


public interface MCMCModel {
	/**
	 * Get the ActivityTask that defines how to execute this MCMC model.
	 */
	public ActivityTask getActivityTask();
	
	/**
	 * Get the simulation results. The results are always available, however they may not be filled.
	 */
	public MCMCResults getResults();
	
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
