package org.drugis.mtc.parameterization;

import org.apache.commons.math.random.RandomGenerator;
import org.drugis.common.stat.EstimateWithPrecision;
import org.drugis.common.stat.Statistics;
import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;

import edu.uci.ics.jung.algorithms.transformation.FoldingTransformerFixed.FoldedEdge;
import edu.uci.ics.jung.graph.UndirectedGraph;

public class ContinuousDataStartingValueGenerator extends AbstractDataStartingValueGenerator {
	/**
	 * Create a deterministic starting value generator.
	 * @param network Network to generate starting values for.
	 */
	public ContinuousDataStartingValueGenerator(Network network, UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph) {
		super(network, cGraph, null, 0.0);
	}
	
	/**
	 * Create a randomized starting value generator.
	 * @param network Network to generate starting values for.
	 * @param rng The random generator to use.
	 * @param scale Scaling factor for the second moment of the error distribution.
	 */
	public ContinuousDataStartingValueGenerator(Network network, UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph, RandomGenerator rng, double scale) {
		super(network, cGraph, rng, scale);
	}

	@Override
	protected EstimateWithPrecision estimateRelativeEffect(Study study, BasicParameter p) {
		Measurement m0 = NetworkModel.findMeasurement(study, p.getBaseline());
		Measurement m1 = NetworkModel.findMeasurement(study, p.getSubject());
		return Statistics.meanDifference(m0.getMean(), m0.getStdDev(), m0.getSampleSize(),
				m1.getMean(), m1.getStdDev(), m1.getSampleSize());
	}

	@Override
	protected EstimateWithPrecision estimateTreatmentEffect(Study study, Treatment treatment) {
		Measurement m = NetworkModel.findMeasurement(study, treatment);
		return new EstimateWithPrecision(m.getMean(), m.getStdDev() / Math.sqrt(m.getSampleSize()));
	}

}
