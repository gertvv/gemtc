package org.drugis.mtc

class Parametrization[M <: Measurement](
		val network: Network[M],
		val basis: FundamentalGraphBasis[Treatment]
) {
	val cycleClass: Map[Cycle[Treatment], Option[(Partition[M], Int)]] = 
		Map[Cycle[Treatment], Option[(Partition[M], Int)]]() ++
			basis.cycles.map(c => classEntry(Cycle(c)))

	val inconsistencyCycles: Map[Partition[M], Set[Cycle[Treatment]]] =
		mapInconsistencyCycles(cycleClass.keySet.toList)

	def inconsistencyClasses: Set[Partition[M]] =
		Set[Partition[M]]() ++ inconsistencyCycles.keySet

	def inconsistencyDegree: Int = inconsistencyClasses.size

	def basicParameters: List[NetworkModelParameter] = null

	def inconsistencyParameters: List[NetworkModelParameter] = null 

	def apply(edge: (Treatment, Treatment)): Map[NetworkModelParameter, Int] =
		null

	private def classEntry(cycle: Cycle[Treatment])
	: (Cycle[Treatment], Option[(Partition[M], Int)]) = {
		val p = Partition(network, cycle).reduce
		if (p.parts.size < 3) (cycle, None)
		else (cycle, Some((p, determineSign(cycle, p))))
	}

	private def determineSign(cycle: Cycle[Treatment], pt: Partition[M])
	: Int = pt.asCycle match {
		case Some(ref) => determineSign(cycle, ref)
		case None => throw new IllegalStateException()
	}

	private def determineSign(cycle: Cycle[Treatment], ref: Cycle[Treatment])
	: Int = determineSign(cycle.rebase(cycle.vertexSeq.head).vertexSeq,
		ref.vertexSeq(1), ref.vertexSeq(ref.vertexSeq.size - 2))

	private def determineSign(cycle: List[Treatment],
		plus: Treatment, minus: Treatment)
	: Int = cycle match {
		case t :: l0 =>
			if (cycle.head == plus) 1
			else if (cycle.head == minus) -1
			else determineSign(l0, plus, minus)
		case Nil => throw new IllegalStateException()
	}

	private def mapInconsistencyCycles(l: List[Cycle[Treatment]])
	: Map[Partition[M], Set[Cycle[Treatment]]] = l match {
		case c :: l0 => addClassOf(c, mapInconsistencyCycles(l0))
		case Nil => Map[Partition[M], Set[Cycle[Treatment]]]()
	}

	private def addClassOf(c: Cycle[Treatment],
		m: Map[Partition[M], Set[Cycle[Treatment]]])
	: Map[Partition[M], Set[Cycle[Treatment]]] = cycleClass(c) match {
		case None => m
		case Some(x) => {
			val p = x._1 // get the partition
			val set =  // retreive the set built so far
				if (m.contains(p)) m(p)
				else Set[Cycle[Treatment]]()
			m + ((p, set + c)) // add the new set
		}
	}
}
