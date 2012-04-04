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
import java.util.List;

import org.drugis.common.stat.DichotomousDescriptives;
import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;

/**
 * Generate priors for an MTC model.
 */
public class PriorGenerator {
	double d_range;
	
	public PriorGenerator(Network network) {
		d_range = calculate(network);
	}

	/**
	 * Give the upper bound S for the prior sd.d ~ U(0, S), where sd.d = sqrt(var.d), var.d the random-effects variance.
	 */
	public double getRandomEffectsSigma() {
		return d_range;
	}
	
	/**
	 * Give the upper bound S for the prior sd.w ~ U(0, S), where sd.w = sqrt(var.w), var.w the inconsistency variance.
	 */
	public double getInconsistencySigma() {
		return d_range;
	}
	
	/**
	 * Give a large standard deviation S to formulate vague normal priors N(0, S^2).
	 */
	public double getVagueNormalSigma() {
		return 15 * d_range;
	}
	
	
	private double calculate(Network network) {
		List<Double> means = null;
		switch (network.getType()) {
		case CONTINUOUS:
			means = means(network, new ContDiff());
			break;
		case RATE:
			means = means(network, new DichDiff());
			break;
		default:
			throw new IllegalArgumentException("Unhandled network type " + network.getType());
		}
		return range(means);
	}
	
	private interface Diff {
		public double calc(Measurement m1, Measurement m2);
	}
	
	public static class DichDiff implements Diff {
		DichotomousDescriptives d_desc = new DichotomousDescriptives(true);
		public double calc(Measurement m1, Measurement m2) {
			return d_desc.logOddsRatio(m1.getResponders(), m1.getSampleSize(), m2.getResponders(), m2.getSampleSize());
		}
	}
	
	public static class ContDiff implements Diff {
		public double calc(Measurement m1, Measurement m2) {
			return m1.getMean() - m2.getMean();
		}
	}

	private double range(List<Double> means) {
		double min = means.get(0);
		double max = means.get(0);
		for (int i = 1; i < means.size(); ++i) {
			if (means.get(i) < min) {
				min = means.get(i);
			} else if (means.get(i) > max) {
				max = means.get(i);
			}
		}
		return max;
	}

	private List<Double> means(Network network, Diff diff) {
		List<Double> list = new ArrayList<Double>();
		for (Study s : network.getStudies()) {
			List<Treatment> treatments = NetworkModel.getTreatments(s);
			for (int i = 0; i < treatments.size() - 1; ++i) {
				for (int j = i + 1; j < treatments.size(); ++j) {
					Measurement m1 = NetworkModel.findMeasurement(s, treatments.get(i));
					Measurement m2 = NetworkModel.findMeasurement(s, treatments.get(j));
					list.add(Math.abs(diff.calc(m1, m2)));
				}
			}
		}
		return list;
	}
}
