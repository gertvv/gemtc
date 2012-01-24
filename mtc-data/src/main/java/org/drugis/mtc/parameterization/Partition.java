package org.drugis.mtc.parameterization;

import java.util.Iterator;
import java.util.Set;

import org.drugis.mtc.graph.GraphUtil;
import org.drugis.mtc.model.Treatment;

import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class Partition {

	public Partition(Set<Part> parts) {
		if (!validPartition(parts)) {
			throw new IllegalArgumentException("Given parts do not form a valid partition");
		}
	}

	private static boolean validPartition(Set<Part> parts) {
		if (parts.isEmpty()) {
			return false;
		}
		if (parts.size() == 1) {
			return parts.iterator().next().getTreatments().size() == 1;
		}
		if (parts.size() == 2) {
			Iterator<Part> iterator = parts.iterator();
			Part p1 = iterator.next();
			Part p2 = iterator.next();
			return p1.getTreatments().equals(p2.getTreatments());
		}
		
		// now try to assemble the parts into a cycle
		UndirectedGraph<Treatment, Part> graph = new UndirectedSparseGraph<Treatment, Part>();
		for (Part p : parts) {
			if (p.getTreatments().size() != 2) { // FIXME: add test for this.
				return false;
			}
			Iterator<Treatment> iterator = p.getTreatments().iterator();
			Treatment t1 = iterator.next();
			Treatment t2 = iterator.next();
			graph.addEdge(p, t1, t2);
		}
		return GraphUtil.isSimpleCycle(graph);
	}

}
