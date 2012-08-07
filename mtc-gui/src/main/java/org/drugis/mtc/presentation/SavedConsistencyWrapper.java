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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.BidiMap;
import org.drugis.mtc.MCMCSettingsCache;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.summary.ConvergenceSummary;
import org.drugis.mtc.summary.MultivariateNormalSummary;
import org.drugis.mtc.summary.QuantileSummary;
import org.drugis.mtc.summary.RankProbabilitySummary;

import edu.uci.ics.jung.graph.util.Pair;

public class SavedConsistencyWrapper<TreatmentType> extends AbstractMTCSavedWrapper<TreatmentType> implements ConsistencyWrapper<TreatmentType> {

	private final MultivariateNormalSummary d_relativeEffectsSummary;
	private final RankProbabilitySummary d_rankProbabilitySummary;
	private List<TreatmentType> d_drugs;

	public SavedConsistencyWrapper(MCMCSettingsCache settings,
			Map<Parameter, QuantileSummary> quantileSummaries,
			Map<Parameter, ConvergenceSummary> convergenceSummaries,
			MultivariateNormalSummary relativeEffectsSummary, 
			RankProbabilitySummary rankProbabilitySummary, 
			List<TreatmentType> drugs, 
			BidiMap<TreatmentType, Treatment> treatmentMap) {
		super(settings, quantileSummaries, convergenceSummaries, treatmentMap);
		d_relativeEffectsSummary = relativeEffectsSummary;
		d_rankProbabilitySummary = rankProbabilitySummary;
		
		d_drugs = drugs;
		List<Pair<TreatmentType>> relEffects = getRelativeEffectsList();
		Parameter[] parameters = new Parameter[relEffects.size()]; 
		for (int i = 0; i < relEffects.size(); ++i) {
			Pair<TreatmentType> relEffect = relEffects.get(i);
			parameters[i] = getRelativeEffect(relEffect.getFirst(), relEffect.getSecond());
		}
	}

	@Override
	public MultivariateNormalSummary getRelativeEffectsSummary() {
		return d_relativeEffectsSummary;
	}

	@Override
	public RankProbabilitySummary getRankProbabilities() {
		return d_rankProbabilitySummary;
	}

	@Override
	public List<Pair<TreatmentType>> getRelativeEffectsList() {
		List<Pair<TreatmentType>> list = new ArrayList<Pair<TreatmentType>>(d_drugs.size() - 1); // first TreatmentDefinition is baseline-> excluded
		for (int i = 0; i < d_drugs.size() - 1; ++i) {
			Pair<TreatmentType> relEffect = new Pair<TreatmentType>(d_drugs.get(0), d_drugs.get(i + 1));
			list.add(relEffect);
		}
		return list;
	}

	@Override
	public String getDescription() {
		return "Consistency Model";
	}
}
