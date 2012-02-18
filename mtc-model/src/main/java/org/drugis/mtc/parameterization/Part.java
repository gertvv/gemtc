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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;

public class Part {

	private Set<Treatment> d_treatments = new HashSet<Treatment>();
	private Set<Study> d_studies;

	/**
	 * A part represents an undirected comparison measured by a set of studies.
	 * Hence, (new Part(ta, tb, s)).equals(new Part(tb, ta, s)).
	 */
	public Part(final Treatment t1, final Treatment t2, final Set<Study> studies) {
		if (studies.isEmpty()) {
			throw new IllegalArgumentException("The given list of studies may not be empty.");
		}
		if (CollectionUtils.exists(studies, new Predicate<Study>() {
				public boolean evaluate(Study s) {
					return !s.containsTreatment(t1) || !s.containsTreatment(t2);
				}})) {
			throw new IllegalArgumentException("All studies must contain both treatments");
		}
		
		d_treatments.add(t1);
		d_treatments.add(t2);
		d_studies = studies;
	}
	
	public Part(Treatment ta, Treatment tb, Collection<Study> studies) {
		this(ta, tb, new HashSet<Study>(studies));
	}

	public Set<Treatment> getTreatments() {
		return Collections.unmodifiableSet(d_treatments);
	}
	
	public Set<Study> getStudies() {
		return Collections.unmodifiableSet(d_studies);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Part) {
			Part other = (Part) obj;
			return d_treatments.equals(other.d_treatments) && d_studies.equals(other.d_studies);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return d_treatments.hashCode() * 31 + d_studies.hashCode();
	}
	
	@Override
	public String toString() {
		return "Part(" + d_treatments.toString() + ", " + d_studies.toString() + ")";
	}
}
