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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.math3.random.RandomGenerator;
import org.drugis.common.stat.EstimateWithPrecision;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.util.DerSimonianLairdPooling;

import edu.uci.ics.jung.graph.util.Pair;

abstract public class AbstractDataStartingValueGenerator implements StartingValueGenerator {
	protected final Network d_network;
	private final PriorGenerator d_priorGen;
	private final RandomGenerator d_rng;
	private final double d_scale;
	
	public static StartingValueGenerator create(Network network, RandomGenerator rng, double scale) {
		switch (network.getType()) {
		case CONTINUOUS:
			return new ContinuousDataStartingValueGenerator(network, rng, scale);
		case RATE:
			return new DichotomousDataStartingValueGenerator(network, rng, scale);
		default:
			throw new IllegalArgumentException("Don't know how to generate starting values for " + network.getType() + " data");					
		}
	}
	
	public AbstractDataStartingValueGenerator(Network network, RandomGenerator rng, double scale) {
		d_network = network;
		d_priorGen = new PriorGenerator(network);
		d_rng = rng;
		d_scale = scale;
	}

	protected abstract EstimateWithPrecision estimateRelativeEffect(Study study, BasicParameter p);

	protected abstract EstimateWithPrecision estimateTreatmentEffect(Study study, Treatment treatment);

	public double getTreatmentEffect(Study study, Treatment treatment) {
		return generate(estimateTreatmentEffect(study, treatment));
	}

	public double getRelativeEffect(Study study, BasicParameter p) {
		return generate(estimateRelativeEffect(study, p));
	}

	public double getRelativeEffect(BasicParameter p) {
		return generate(getPooledEffect(p));
	}
	
	public double getStandardDeviation() {
		if (d_rng == null) {
			return 0.5 * d_priorGen.getRandomEffectsSigma();
		}
		return d_rng.nextDouble() * d_priorGen.getRandomEffectsSigma();
	}

	private EstimateWithPrecision getPooledEffect(BasicParameter p) {
		return (new DerSimonianLairdPooling(estimateRelativeEffects(p))).getPooled();
	}

	private double generate(EstimateWithPrecision estimate) {
		if (d_rng == null) {
			return estimate.getPointEstimate();
		}
		return estimate.getPointEstimate() + d_rng.nextGaussian() * d_scale * estimate.getStandardError();
	}

	protected List<EstimateWithPrecision> estimateRelativeEffects(final BasicParameter p) {
		final Pair<Treatment> treatments = new Pair<Treatment>(p.getBaseline(), p.getSubject());
		Set<Study> studies = new HashSet<Study>(d_network.getStudies());
		CollectionUtils.filter(studies, new Predicate<Study>() {
			public boolean evaluate(Study study) {
				return study.getTreatments().containsAll(treatments);
			}
		});
		
		List<EstimateWithPrecision> estimates = new ArrayList<EstimateWithPrecision>(studies.size());
		for (Study s : studies) {
			estimates.add(estimateRelativeEffect(s, p));
		}
		return estimates;
	}
}
