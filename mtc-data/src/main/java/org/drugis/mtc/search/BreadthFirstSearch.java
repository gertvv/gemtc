package org.drugis.mtc.search;

import java.util.LinkedList;
import java.util.List;

public class BreadthFirstSearch<State> extends GeneralSearch<State> {
	@Override
	protected void qfn(LinkedList<State> q, List<State> l) {
		q.addAll(l);
	}
}
