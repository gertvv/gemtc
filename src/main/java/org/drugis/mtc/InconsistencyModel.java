package org.drugis.mtc;

import java.util.List;

public interface InconsistencyModel extends MixedTreatmentComparison {
	public List<InconsistencyParameter> getInconsistencyFactors();
	public Estimate getInconsistency(InconsistencyParameter param);
}
