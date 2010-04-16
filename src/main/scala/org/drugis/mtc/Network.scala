package org.drugis.mtc

import org.drugis.mtc.{DichotomousMeasurement => M}

class Network(val treatments: Set[Treatment], val studies: Set[Study[M]]) {
	override def toString = treatments.toString + studies.toString

	val treatmentGraph: UndirectedGraph[Treatment] = {
		var graph = new UndirectedGraph[Treatment](
			Set[(Treatment, Treatment)]())
		for (s <- studies) {
			graph = graph.union(s.treatmentGraph)
		}
		graph
	}

	def supportingEvidence(cycle: UndirectedGraph[Treatment])
		: Set[UndirectedGraph[Treatment]] = {
		Set[UndirectedGraph[Treatment]]() ++ {
			for {s <- studies
				val edges = s.treatmentGraph.intersection(cycle).edgeSet
				if !edges.isEmpty
			} yield new UndirectedGraph[Treatment](edges)
		}
	}

	def evidenceMatrix(cycle: UndirectedGraph[Treatment])
	: Matrix[Boolean] = {
		new Matrix[Boolean](
			{for {s <- supportingEvidence(cycle)} 
			yield s.incidenceVector(cycle.edgeVector)}.toList)
	}

	def evidenceDimensionality(cycle: UndirectedGraph[Treatment]): Int = {
		val m = Matrix.gaussElimGF2(evidenceMatrix(cycle))
		val i = m.elements.findIndexOf(r => !r.contains(true))
		if (i == -1) m.nRows
		else i
	}

	def isInconsistency(cycle: UndirectedGraph[Treatment]): Boolean =
		evidenceDimensionality(cycle) >= 3

	val edgeVector: List[(Treatment, Treatment)] =
		treatmentGraph.edgeVector

	def countInconsistencies(st: Tree[Treatment]): Int =
		inconsistencies(st).size

	def inconsistencies(st: Tree[Treatment])
	: Set[UndirectedGraph[Treatment]] = {
		for {c <- treatmentGraph.fundamentalCycles(st);
			if isInconsistency(c)} yield c
	}

	def treeEnumerator(top: Treatment) =
		SpanningTreeEnumerator.treeEnumerator(treatmentGraph, top)

	private def weight(a: Tree[Treatment]): Int = {
		{for {c <- treatmentGraph.fundamentalCycles(a)} yield c.edgeSet.size
		}.reduceLeft((a, b) => a + b)
	}

	private def compare(a: Tree[Treatment], b: Tree[Treatment]): Int = {
		val icdf = countInconsistencies(a) - countInconsistencies(b)
		if (icdf == 0) weight(b) - weight(a)
		else icdf
	}

	private def better(a: Tree[Treatment], b: Tree[Treatment]): Boolean = {
		compare(a, b) > 0
	}

	def bestSpanningTree(top: Treatment): Tree[Treatment] = {
		treeEnumerator(top).reduceLeft((a, b) => if (better(a, b)) a else b)
	}

	def filterTreatments(ts: Set[Treatment]): Network = {
		if (!ts.subsetOf(treatments))
			throw new RuntimeException(ts + " not a subset of " + treatments)

		new Network(ts,
			studies.filter(s => s.treatments.intersect(ts).size > 1).map(
				s => filterStudy(s, ts)))
	}

	private def filterStudy(s: Study[M], ts: Set[Treatment]): Study[M] =
		if (s.treatments.intersect(ts) == s.treatments) s
		else new Study[M](s.id, s.measurements.filter(x => ts.contains(x._1)))
}

object Network {
	def fromXML(node: scala.xml.Node): Network =  {
		val treatments = treatmentsFromXML((node \ "treatments")(0))
		new Network(Set[Treatment]() ++ treatments.values, 
			studiesFromXML((node \ "studies")(0), treatments))
	}

	def treatmentsFromXML(n: scala.xml.Node): Map[String, Treatment] =
		Map[String, Treatment]() ++
		{for (node <- n \ "treatment") yield ((node \ "@id").text, Treatment.fromXML(node))}

	def studiesFromXML(n: scala.xml.Node, treatments: Map[String, Treatment]): Set[Study[M]] =
		Set[Study[M]]() ++
		{for (node <- n \ "study") yield Study.fromXML(node, treatments)}
}
