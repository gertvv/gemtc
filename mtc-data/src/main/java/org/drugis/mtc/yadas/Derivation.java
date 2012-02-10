package org.drugis.mtc.yadas;

import java.util.Map;
import java.util.Map.Entry;

import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.Parameter;

public class Derivation {
	private final Map<? extends Parameter, Integer> d_pmtz;
	
	public Derivation(Map<? extends Parameter, Integer> pmtz) {
		assert(!pmtz.isEmpty());
		d_pmtz = pmtz;	
	}
	

	public double[] calculate(MCMCResults results, int c) {
		double[] result = new double[results.getNumberOfSamples()];
		for (int i = 0; i < result.length; ++i) {
			result[i] = calculate(results, c, i);
		}
		return result;
	}
	
	public double calculate(MCMCResults results, int c, int i) {
		double val = 0.0;
		for (Entry<? extends Parameter, Integer> e : d_pmtz.entrySet()) {
			val += e.getValue() * results.getSample(results.findParameter(e.getKey()), c, i);
		}
		return val;
	}
}
