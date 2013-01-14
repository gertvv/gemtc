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
import org.drugis.common.stat.EstimateWithPrecision;
import org.drugis.common.stat.Statistics;
import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;


public class ContinuousDataStartingValueGenerator extends AbstractDataStartingValueGenerator {
	/**
	 * Create a deterministic starting value generator.
	 * @param network Network to generate starting values for.
	 */
	public ContinuousDataStartingValueGenerator(Network network) {
		super(network, null, 0.0);
	}
	
	/**
	 * Create a randomized starting value generator.
	 * @param network Network to generate starting values for.
	 * @param rng The random generator to use.
	 * @param scale Scaling factor for the second moment of the error distribution.
	 */
	public ContinuousDataStartingValueGenerator(Network network, RandomGenerator rng, double scale) {
		super(network, rng, scale);
	}

	@Override
	protected EstimateWithPrecision estimateRelativeEffect(Study study, BasicParameter p) {
		Measurement m0 = NetworkModel.findMeasurement(study, p.getBaseline());
		Measurement m1 = NetworkModel.findMeasurement(study, p.getSubject());
		return Statistics.meanDifference(m0.getMean(), m0.getStdDev(), m0.getSampleSize(),
				m1.getMean(), m1.getStdDev(), m1.getSampleSize());
	}

	@Override
	protected EstimateWithPrecision estimateTreatmentEffect(Study study, Treatment treatment) {
		Measurement m = NetworkModel.findMeasurement(study, treatment);
		return new EstimateWithPrecision(m.getMean(), m.getStdDev() / Math.sqrt(m.getSampleSize()));
	}

}
