package org.drugis.mtc;

import java.util.List;

public interface ConsistencyModel extends MixedTreatmentComparison {
	public double rankProbability(Treatment t, int r);
}
