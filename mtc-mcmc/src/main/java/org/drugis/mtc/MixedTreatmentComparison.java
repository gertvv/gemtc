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

import org.drugis.mtc.model.Treatment;

/**
 * A MixedTreatmentComparison estimates the relative effects of a set of
 * treatments given an evidence network. The estimates are only provided after
 * the model has been run (by executing the ActivityModel).
 */
public interface MixedTreatmentComparison extends MCMCModel {
	/**
	 * Get the relative effect MCMCParameter.
	 * @return The effect parameter.
	 * @param base The treatment to use as baseline.
	 * @param subj The treatment to use as alternative.
	 * @throws IllegalArgumentException if one of the treatments is not
	 * present in the evidence network.
	 * @throws IllegalStateException if the MTC is not ready.
	 */
	public Parameter getRelativeEffect(Treatment base, Treatment subj);

	/**
	 * @return Get the random effects standard deviation.
	 */
	public Parameter getRandomEffectsStandardDeviation();
}
