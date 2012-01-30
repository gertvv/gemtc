package org.drugis.mtc.search;

import java.util.List;

public interface SearchProblem<State> {
	public State getInitialState();
	public boolean isGoal(State state);
	public List<State> getSuccessors(State state);
}
