package org.drugis.mtc;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.apache.commons.collections15.bidimap.UnmodifiableBidiMap;
import org.drugis.common.EqualsUtil;
import org.drugis.mtc.util.ScalaUtil;

public class NetworkBuilder<M extends Measurement, TreatmentType> {
	private static class MKey {
		public final String studyId;
		public final Treatment treatment;
		
		public MKey(String studyId, Treatment treatment) {
			this.studyId = studyId;
			this.treatment = treatment;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o != null && o instanceof MKey) {
				MKey other = (MKey) o;
				return EqualsUtil.equal(studyId, other.studyId) && EqualsUtil.equal(treatment, other.treatment);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return studyId.hashCode() * 31 + treatment.hashCode();
		}
	}

	private BidiMap<TreatmentType, Treatment> d_treatmentMap = new DualHashBidiMap<TreatmentType, Treatment>();
	private Map<MKey, M> d_measurementMap = new HashMap<MKey, M>();

	public Network<M> buildNetwork() {
		return new Network<M>(ScalaUtil.toScalaSet(getTreatments()), ScalaUtil.toScalaSet(getStudies()));
	}
	
	public BidiMap<TreatmentType, Treatment> getTreatmentMap() {
		return UnmodifiableBidiMap.decorate(d_treatmentMap);
	}
	
	protected void add(String studyId, Treatment t, M measurement) {
		MKey key = new MKey(studyId, t);
		if (d_measurementMap.containsKey(key)) {
			throw new IllegalArgumentException("Study/Treatment combination already mapped.");
		}
		d_measurementMap.put(key, measurement);
	}

	protected Treatment makeTreatment(TreatmentType id) {
		if (!d_treatmentMap.containsKey(id)) {
			d_treatmentMap.put(id, new Treatment(id.toString()));
		}
		return d_treatmentMap.get(id);
	}

	private Set<Study<M>> getStudies() {
		Set<String> ids = new HashSet<String>();
		for (MKey key : d_measurementMap.keySet()) {
			ids.add(key.studyId);
		}
		Set<Study<M>> studies = new HashSet<Study<M>>();
		for (String id : ids) {
			studies.add(getStudy(id));
		}
		return studies;
	}

	private Study<M> getStudy(String id) {
		Map<Treatment, M> measurements = new HashMap<Treatment, M>();
		for (MKey key : d_measurementMap.keySet()) {
			if (key.studyId.equals(id)) {
				measurements.put(key.treatment, d_measurementMap.get(key));
			}
		}
		return new Study<M>(id, ScalaUtil.toScalaMap(measurements));
	}

	private Collection<Treatment> getTreatments() {
		return d_treatmentMap.values();
	}
}
