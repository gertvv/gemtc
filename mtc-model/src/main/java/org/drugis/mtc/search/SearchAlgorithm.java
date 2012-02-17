package org.drugis.mtc.search;

public interface SearchAlgorithm<State> {
	/**
	 * Given the problem, find the solution state.
	 * @return The solution, or null if no solution found.
	 */
	public State search(SearchProblem<State> problem);
}
