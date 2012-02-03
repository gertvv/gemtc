package org.drugis.mtc.parameterization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.math.random.RandomGenerator;
import org.drugis.common.stat.EstimateWithPrecision;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.util.DerSimonianLairdPooling;

import edu.uci.ics.jung.graph.util.Pair;

abstract public class AbstractDataStartingValueGenerator implements StartingValueGenerator {
	protected final Network d_network;
	private final RandomGenerator d_rng;
	private final double d_scale;
	
	public AbstractDataStartingValueGenerator(Network network, RandomGenerator rng, double scale) {
		d_network = network;
		d_rng = rng;
		d_scale = scale;
	}

	protected abstract EstimateWithPrecision estimateRelativeEffect(Study study, BasicParameter p);

	protected abstract EstimateWithPrecision estimateTreatmentEffect(Study study, Treatment treatment);

	@Override
	public double getTreatmentEffect(Study study, Treatment treatment) {
		return generate(estimateTreatmentEffect(study, treatment));
	}

	@Override
	public double getRelativeEffect(Study study, BasicParameter p) {
		return generate(estimateRelativeEffect(study, p));
	}

	@Override
	public double getRelativeEffect(BasicParameter p) {
		EstimateWithPrecision pooled = (new DerSimonianLairdPooling(estimateRelativeEffects(p))).getPooled();
		return generate(pooled);
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
