/*
 * This file is part of the GeMTC software for MTC model generation and
 * analysis. GeMTC is distributed from http://drugis.org/gemtc.
 * Copyright (C) 2009-2012 Gert van Valkenhoef.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
	
	public final State search(SearchProblem<State> problem) {
		LinkedList<State> queue = new LinkedList<State>();
		queue.add(problem.getInitialState());
		while (!queue.isEmpty()) {
			State s = queue.removeFirst();
			if (problem.isGoal(s)) {
				return s;
			}
			qfn(queue, problem.getSuccessors(s));
		}
		return null;
	}
}
