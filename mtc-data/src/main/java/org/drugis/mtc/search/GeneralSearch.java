package org.drugis.mtc.search;

import java.util.LinkedList;
import java.util.List;

public abstract class GeneralSearch<State> implements SearchAlgorithm<State> {
	/**
	 * Add the given items to the queue.
	 * @param q The queue.
	 * @param l A list of items to be added.
	 */
	protected abstract void qfn(LinkedList<State> q, List<State> l);
	
	@Override
	public final State search(SearchProblem<State> problem) {
		LinkedList<State> queue = new LinkedList<State>();
		queue.add(problem.getInitialState());
		while (!queue.isEmpty()) {
			State s = queue.pop();
			if (problem.isGoal(s)) {
				return s;
			}
			qfn(queue, problem.getSuccessors(s));
		}
		return null;
	}
}
