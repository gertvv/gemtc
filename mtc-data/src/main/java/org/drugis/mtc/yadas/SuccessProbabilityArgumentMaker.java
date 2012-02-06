package org.drugis.mtc.yadas;

import java.util.List;

import org.drugis.mtc.model.Treatment;
import static org.drugis.common.stat.Statistics.ilogit;

import edu.uci.ics.jung.graph.util.Pair;

/**
 * ArgumentMaker for individual treatment success probabilities within studies.
 * p_i,k = ilogit(theta_i,k) ; theta_i,k = mu_i + delta_i,b(i),k
 */
public class SuccessProbabilityArgumentMaker extends ThetaArgumentMaker {
	public SuccessProbabilityArgumentMaker(List<Treatment> treatments, List<List<Pair<Treatment>>> studyPmtz, int muIdx, int deltaIdx) {
		super(treatments, studyPmtz, muIdx, deltaIdx);
	}
	
	@Override
	protected double theta(int tIdx, double[][] data) {
		return ilogit(super.theta(tIdx, data));
	}
}