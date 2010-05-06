package org.drugis.mtc

class Network[M <: Measurement](
		val treatments: Set[Treatment], val studies: Set[Study[M]]) {
	override def toString = treatments.toString + studies.toString

	val measurementType: Class[M] =
		studies.find(_ => true) match {
			case Some(x) => x.measurements.values.find(_ => true) match {
				case Some(y) => y.getClass.asInstanceOf[Class[M]]
				case None => null
			}
			case None => null
		}

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

	private def invert(e: (Treatment, Treatment)) = (e._2, e._1)

	def walkCycle(cycle: UndirectedGraph[Treatment])
	: List[(Treatment, Treatment)] = {
		def aux(cycle: UndirectedGraph[Treatment],
			l0: List[(Treatment, Treatment)])
		: List[(Treatment, Treatment)] = {
			if (l0.size > 1 && l0.head == l0.last) l0
			else {
				val edge = (cycle.edgesFrom(l0.head._2) - invert(l0.head)
					).toList(0)
				aux(cycle, edge :: l0)
			}
		}
		val edge = cycle.edgesFrom(cycle.vertexSet.toList(0)).toList(0)
		aux(cycle, List(edge))
	}

	def supportingStudies(edge: (Treatment, Treatment)): Set[Study[M]] = {
		studies.filter(s => s.treatmentGraph.containsEdge(edge))
	}

	def evidenceDimensionality(cycle: List[(Treatment, Treatment)]): Int = {
		def aux(l: List[(Treatment, Treatment)]): Int = l match {
			case e2 :: (e1 :: l1) => aux(e1 :: l1) + {
				if (supportingStudies(e2) != supportingStudies(e1)) 1
				else 0
			}
			case e2 :: Nil => 0
			case Nil => 0
		}
		if (cycle.size < 3 || cycle.head != cycle.last)
			throw new IllegalArgumentException("Not a cycle: " + cycle)
		aux(cycle)
	}

	def evidenceDimensionality(cycle: UndirectedGraph[Treatment]): Int = {
		evidenceDimensionality(walkCycle(cycle))
	}

	def isInconsistency(cycle: UndirectedGraph[Treatment]): Boolean =
		evidenceDimensionality(cycle) >= 3

	val edgeVector: List[(Treatment, Treatment)] =
		treatmentGraph.edgeVector

	val countFunctionalParameters: Int =
		treatmentGraph.edgeSet.size - treatmentGraph.vertexSet.size + 1

	def countInconsistencies(st: Tree[Treatment]): Int =
		inconsistencies(st).size

	def inconsistencies(st: Tree[Treatment])
	: Set[UndirectedGraph[Treatment]] = {
		for {c <- treatmentGraph.fundamentalCycles(st);
			if isInconsistency(c)} yield c
	}

	def treeEnumerator(top: Treatment) =
		SpanningTreeEnumerator.treeEnumerator(treatmentGraph, top)

	private def compare(a: Tree[Treatment], b: Tree[Treatment]): Int = {
		countInconsistencies(a) - countInconsistencies(b)
	}

	private def better(a: Tree[Treatment], b: Tree[Treatment]): Boolean = {
		compare(a, b) > 0
	}

	def bestSpanningTree(top: Treatment): Tree[Treatment] = {
		var max = 0
		var best: Tree[Treatment] = null
		for (tree <- treeEnumerator(top)) {
			val incons = countInconsistencies(tree)
			if (incons >= max) {
				// FIXME: check for existence of baseline assignment
				max = incons
				best = tree
			}
			if (max == countFunctionalParameters) {
				return best
			}
		}
		best
	}

	def filterTreatments(ts: Set[Treatment]): Network[M] = {
		if (!ts.subsetOf(treatments))
			throw new RuntimeException(ts + " not a subset of " + treatments)

		new Network[M](ts,
			studies.filter(s => s.treatments.intersect(ts).size > 1).map(
				s => filterStudy(s, ts)))
	}

	private def filterStudy(s: Study[M], ts: Set[Treatment]): Study[M] =
		if (s.treatments.intersect(ts) == s.treatments) s
		else new Study[M](s.id, s.measurements.filter(x => ts.contains(x._1)))
}

object Network {
	/*
	def fromXML(node: scala.xml.Node): Network[DichotomousMeasurement] = {
		dichFromXML(node)
	} */
	def fromXML(node: scala.xml.Node): Network[_ <: Measurement] = {
		node.attribute("type") match {
			case Some(x) => x.text match {
				case "continuous" => contFromXML(node)
				case "dichotomous" => dichFromXML(node)
				case _ => throw new RuntimeException(
					"Unsupported network type " + x.text)
			}
			case None => dichFromXML(node)
		}
	}

	def dichFromXML(node: scala.xml.Node): Network[DichotomousMeasurement] = {
		val treatments = treatmentsFromXML((node \ "treatments")(0))
		new Network(Set[Treatment]() ++ treatments.values, 
			studiesFromXML((node \ "studies")(0), treatments,
				DichotomousMeasurement.fromXML))
	}

	def contFromXML(node: scala.xml.Node): Network[ContinuousMeasurement] = {
		val treatments = treatmentsFromXML((node \ "treatments")(0))
		new Network(Set[Treatment]() ++ treatments.values, 
			studiesFromXML((node \ "studies")(0), treatments,
				ContinuousMeasurement.fromXML))
	}

	def treatmentsFromXML(n: scala.xml.Node): Map[String, Treatment] =
		Map[String, Treatment]() ++
		{for (node <- n \ "treatment") yield ((node \ "@id").text, Treatment.fromXML(node))}

	def studiesFromXML[M <: Measurement](n: scala.xml.Node,
			treatments: Map[String, Treatment],
			measReader: (scala.xml.Node, Map[String, Treatment]) => M)
	: Set[Study[M]] =
		Set[Study[M]]() ++
		{for (node <- n \ "study") yield Study.fromXML(node, treatments, measReader)}
}
