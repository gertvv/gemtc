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

package org.drugis.mtc.parameterization;


import org.apache.commons.math3.random.RandomGenerator;
import org.drugis.common.stat.DichotomousDescriptives;
import org.drugis.common.stat.EstimateWithPrecision;
import org.drugis.common.stat.Statistics;
import org.drugis.mtc.data.DataType;
import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;



/**
 * Generate starting values for a network with dichotomous data.
 */
public class DichotomousDataStartingValueGenerator extends AbstractDataStartingValueGenerator {
	/**
	 * Create a deterministic starting value generator.
	 * @param network Network to generate starting values for.
	 */
	public DichotomousDataStartingValueGenerator(Network network) {
		super(network, null, 0.0);
		if (!network.getType().equals(DataType.RATE)) {
			throw new IllegalArgumentException("The network must be RATE");
		}
	}
	
	/**
	 * Create a randomized starting value generator.
	 * @param network Network to generate starting values for.
	 * @param rng The random generator to use.
	 * @param scale Scaling factor for the second moment of the error distribution.
	 */	
	public DichotomousDataStartingValueGenerator(Network network, RandomGenerator rng, double scale) {
		super(network, rng, scale);
		if (!network.getType().equals(DataType.RATE)) {
			throw new IllegalArgumentException("The network must be RATE");
		}
	}
	
	@Override
	protected EstimateWithPrecision estimateTreatmentEffect(Study study, Treatment treatment) {
		Measurement m = NetworkModel.findMeasurement(study, treatment);
		final DichotomousDescriptives descriptives = new DichotomousDescriptives(true);
		double mean = descriptives.logOdds(m.getResponders(), m.getSampleSize());
		double se = descriptives.logOddsError(m.getResponders(), m.getSampleSize());
		return new EstimateWithPrecision(mean, se);
	}

	@Override
	protected EstimateWithPrecision estimateRelativeEffect(Study study, BasicParameter p) {
		Measurement m0 = NetworkModel.findMeasurement(study, p.getBaseline());
		Measurement m1 = NetworkModel.findMeasurement(study, p.getSubject());
		return Statistics.logOddsRatio(m0.getResponders(), m0.getSampleSize(), m1.getResponders(), m1.getSampleSize(), true);
	}

}
