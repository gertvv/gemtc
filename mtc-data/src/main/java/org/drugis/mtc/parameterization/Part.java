package org.drugis.mtc.parameterization;

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
	 * @param t1 
	 * @param t2
	 * @param studies
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
}
