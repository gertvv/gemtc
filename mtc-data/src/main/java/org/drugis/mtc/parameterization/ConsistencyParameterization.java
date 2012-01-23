package org.drugis.mtc.parameterization;

import java.util.Collection;

import org.drugis.mtc.graph.MinimumDiameterSpanningTree;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;

import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.UndirectedGraph;

public class ConsistencyParameterization implements Parameterization {
	/**
	 * Find the minimum diameter spanning tree for the comparison graph.
	 * @param comparisonGraph
	 * @return The minimum diameter spanning tree.
	 */
	public static Tree<Treatment, Collection<Study>> findSpanningTree(UndirectedGraph<Treatment, Collection<Study>> comparisonGraph) {
		MinimumDiameterSpanningTree<Treatment, Collection<Study>> mdst = new MinimumDiameterSpanningTree<Treatment, Collection<Study>>(comparisonGraph);
		return mdst.getMinimumDiameterSpanningTree();
	}
}
