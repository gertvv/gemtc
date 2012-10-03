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

package org.drugis.mtc.model;

import org.apache.commons.collections15.Transformer;
import org.drugis.mtc.data.DataType;

public class ContinuousNetworkBuilder<TreatmentType> extends NetworkBuilder<TreatmentType> {
	public static ContinuousNetworkBuilder<Treatment> createSimple() {
		return new ContinuousNetworkBuilder<Treatment>(
				new NetworkBuilder.TreatmentIdTransformer(),
				new NetworkBuilder.TreatmentDescriptionTransformer());
	}
	
	public ContinuousNetworkBuilder() {
		super(DataType.CONTINUOUS);
	}
	
	public ContinuousNetworkBuilder(Transformer<TreatmentType, String> treatmentToIdString, Transformer<TreatmentType, String> treatmentToDescription) {
		super(treatmentToIdString, treatmentToDescription, DataType.CONTINUOUS);
	}
	
	@Override
	public Network buildNetwork() {
		final Network network = super.buildNetwork();
		return network;
	}
	
	public void add(String studyId, TreatmentType treatmentId, double mean, double stdDev, int sampleSize) {
		Treatment t = makeTreatment(treatmentId);
		add(studyId, t, new Measurement(t, mean, stdDev, sampleSize));
	}
}
