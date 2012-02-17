package org.drugis.mtc.parameterization;

import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;

public class PriorStartingValueGenerator implements StartingValueGenerator {
	final PriorGenerator d_priorGen;
	
	public PriorStartingValueGenerator(PriorGenerator priorGen) {
		d_priorGen = priorGen;
	}
	
	public double getRelativeEffect(Study study, BasicParameter p) {
		return 0.0;
	}

	public double getRelativeEffect(BasicParameter p) {
		return 0.0;
	}

	public double getTreatmentEffect(Study study, Treatment treatment) {
		return 0.0;
	}

	public double getStandardDeviation() {
		return d_priorGen.getRandomEffectsSigma() / 2.0;
	}
}