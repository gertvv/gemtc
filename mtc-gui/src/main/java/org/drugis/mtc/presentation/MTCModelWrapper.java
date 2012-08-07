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

import org.drugis.mtc.MixedTreatmentComparison;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.model.Treatment;

public interface MTCModelWrapper<TreatmentType> extends MCMCModelWrapper {
	/**
	 * @see org.drugis.mtc.MixedTreatmentComparison#getRelativeEffect(org.drugis.mtc.model.Treatment, org.drugis.mtc.model.Treatment)
	 */
	public Parameter getRelativeEffect(TreatmentType a, TreatmentType b);
	
	/**
	 * @see org.drugis.mtc.MixedTreatmentComparison#getRandomEffectsVariance()
	 */
	public Parameter getRandomEffectsVariance();
	
	/**
	 * Get the underlying MCMC model.
	 * Can not be called when {@link #isSaved()} is true.
	 */
	@Override
	public MixedTreatmentComparison getModel();
	
	/**
	 * @return The TreatmentType for which the Treatment t was constructed.
	 */
	public TreatmentType reverseMap(Treatment t);
	
	/**
	 * @return The Treatment that was constructed for the TreatmentType t.
	 */
	public Treatment forwardMap(TreatmentType t);
}