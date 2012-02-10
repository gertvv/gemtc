package org.drugis.mtc.yadas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.drugis.mtc.model.Treatment;

import edu.uci.ics.jung.graph.util.Pair;

import gov.lanl.yadas.ArgumentMaker;

/**
 * ArgumentMaker for (a row of the) variance-covariance matrix of relative effects Gaussian.
 */
public class SigmaRowArgumentMaker implements ArgumentMaker {
	private final int d_sigmaIdx;
	private final int d_row;
	private int d_nRows;
	private int d_from;
	private int d_to;
	
	/**
	 * Create a list of SigmaRowArgumentMakers, one for each row of the var/covar matrix.
	 * @param studyPmtz The study parameterization: (baseline, subject) pairs, forming a tree.
	 * @param sigmaIdx The index where the standard deviation parameter is stored.
	 */
	public static List<ArgumentMaker> createMatrixArgumentMaker(List<List<Pair<Treatment>>> studyPmtz, int sigmaIdx) {
		List<ArgumentMaker> makers = new ArrayList<ArgumentMaker>();
		int row = 0;
		for (List<Pair<Treatment>> list : studyPmtz) {
			for (int i = 0; i < list.size(); ++i) {
				makers.add(new SigmaRowArgumentMaker(studyPmtz, row, sigmaIdx));
				++row;
			}
		}
		return makers;
	}
	
	/**
	 * @param studyPmtz The study parameterization: (baseline, subject) pairs, forming a tree.
	 * @param row The row of the var/covar matrix to generate values for.
	 * @param sigmaIdx The index where the standard deviation parameter is stored.
	 */
	public SigmaRowArgumentMaker(List<List<Pair<Treatment>>> studyPmtz, int row, int sigmaIdx) {
		d_row = row;
		d_sigmaIdx = sigmaIdx;

		d_nRows = 0;
		for (List<Pair<Treatment>> list : studyPmtz) {
			if (row >= d_nRows && row < d_nRows + list.size()) {
				d_from = d_nRows;
				d_to = d_nRows + list.size();
			}
			d_nRows += list.size();
		}
	}
	
	/**
	 * Calculate "the argument": a row of the var/covar matrix.
	 * data[sigmaIdx][0] should contain the standard deviation
	 * @return row rowIdx of the var/covar matrix.
	 */
	@Override
	public double[] getArgument(double[][] data) {
		double sd = data[d_sigmaIdx][0];
		double var = sd * sd;
		double cov = var / 2;
		
		// Initialize the row to 0.0
		double[] arr = new double[d_nRows];

		// Covariance should != 0.0 only for the block of relative effects that this relative effect belongs to
		Arrays.fill(arr, d_from, d_to, cov);

		// Set the variance at (row, row).
		arr[d_row] = var;

		return arr;
	}
}