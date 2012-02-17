package org.drugis.mtc;

public class ResultsUtil {
	public static double[] getSamples(MCMCResults r, int p, int c) {
		double[] samples = new double[r.getNumberOfSamples()];
		for (int i = 0; i < r.getNumberOfSamples(); ++i) {
			samples[i] = r.getSample(p, c, i);
		}
		return samples;
	}

	public static double[] getSamples(MCMCResults r, Parameter p, int c) {
		return getSamples(r, r.findParameter(p), c);
	}
}
