package org.drugis.mtc

class Parametrization[M <: Measurement](
		val network: Network[M],
		val basis: FundamentalGraphBasis[Treatment]
) {
	val cycles: Set[Cycle[Treatment]] = 
		basis.cycles.map(c => Cycle(c))

	val cycleClass: Map[Cycle[Treatment], Option[(Partition[M], Int)]] = 
		Map[Cycle[Treatment], Option[(Partition[M], Int)]]() ++
			cycles.map(c => classEntry(c))

	val inconsistencyCycles: Map[Partition[M], Set[Cycle[Treatment]]] =
		mapInconsistencyCycles(cycleClass.keySet.toList)

	def inconsistencyClasses: Set[Partition[M]] =
		Set[Partition[M]]() ++ inconsistencyCycles.keySet

	def inconsistencyDegree: Int = inconsistencyClasses.size

	def basicParameters: List[BasicParameter] =
		sort(basis.treeEdges).map(e => new BasicParameter(e._1, e._2))

	def inconsistencyParameters: List[InconsistencyParameter] = 
		cycleSort(inconsistencyClasses.map(asCycle _)).map(c =>
			new InconsistencyParameter(c.vertexSeq))

	/**
	 * Parametrization of d.t1.t2 (effect of t2 relative to t1).
	 */
	def apply(t1: Treatment, t2: Treatment): Map[NetworkModelParameter, Int] =
		param(t1, t2)

	private def classEntry(cycle: Cycle[Treatment])
	: (Cycle[Treatment], Option[(Partition[M], Int)]) = {
		val p = Partition(network, cycle).reduce
		if (p.parts.size < 3) (cycle, None)
		else (cycle, Some((p, determineSign(cycle, p))))
	}

	private def asCycle(pt: Partition[M]): Cycle[Treatment] = pt.asCycle match {
		case Some(cycle) => cycle
		case None => throw new IllegalStateException()
	}

	private def determineSign(cycle: Cycle[Treatment], pt: Partition[M])
	: Int = determineSign(cycle, asCycle(pt))

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

	private def sort(edges: Set[(Treatment, Treatment)])
	: List[(Treatment, Treatment)] =
		edges.toList.sort(
			(a, b) => if (a._1 == b._1) a._2 < b._2 else a._1 < b._1)

	private def cycleSort(cycles: Set[Cycle[Treatment]])
	: List[Cycle[Treatment]] =
		cycles.toList.sort((a, b) => compare(a.vertexSeq, b.vertexSeq))

	private def compare(l: List[Treatment], r: List[Treatment]): Boolean = {
		l match {
			case Nil => r match {
				case Nil => false // l and r equal
				case _ => true // l shorter than r, equal so far
			}
			case x :: l0 => r match {
				case Nil => false // l longer than r, equal so far
				case y :: r0 =>
					if (x < y) true
					else if (y < x) false
					else compare(l0, r0)
			}
		}
	}

	private def param(a: Treatment, b: Treatment)
	: Map[NetworkModelParameter, Int] = {
		if (basis.treeEdges contains (a, b)) basicParam(a, b)
		else if (basis.treeEdges contains (b, a)) negate(basicParam(b, a))
		else functionalParam(a, b)
	}

	private def functionalParam(a: Treatment, b: Treatment)
	: Map[NetworkModelParameter, Int] = {
		val r = basis.tree.commonAncestor(a, b)
		add(add(negate(pathParam(basis.tree.path(r, a))),
			pathParam(basis.tree.path(r, b))),
			inconsParam(a, b))
	}

	private def inconsParam(a: Treatment, b: Treatment)
	: Map[NetworkModelParameter, Int] = {
		if (a < b) inconsParam(Cycle(basis.tree.cycle(a, b)))
		else negate(inconsParam(Cycle(basis.tree.cycle(b, a))))
	}

	private def inconsParam(cycle: Cycle[Treatment])
	: Map[NetworkModelParameter, Int] = cycleClass(cycle) match {
		case None => emptyParam()
		case Some(cls) => Map[NetworkModelParameter, Int](
			(new InconsistencyParameter(asCycle(cls._1).vertexSeq), cls._2))
	}

	private def pathParam(path: List[Treatment])
	: Map[NetworkModelParameter, Int] = {
		if (path.size < 2) emptyParam()
		else add(param(path(0), path(1)), pathParam(path.tail))
	}

	private def basicParam(a: Treatment, b: Treatment) =
		Map[NetworkModelParameter, Int]((new BasicParameter(a, b), 1))

	private def negate(p: Map[NetworkModelParameter, Int]) =
		p.transform((a, b) => -b)

	private def add(p: Map[NetworkModelParameter, Int],
			q: Map[NetworkModelParameter, Int])
	: Map[NetworkModelParameter, Int] = {
		emptyParam() ++
		(for {x <- (p.keySet ++ q.keySet)
		} yield (x, getOrZero(p, x) + getOrZero(q, x)))
	}

	private def emptyParam() = Map[NetworkModelParameter, Int]()

	private def getOrZero(p: Map[NetworkModelParameter, Int],
			x: NetworkModelParameter): Int =
	p.get(x) match {
		case None => 0
		case Some(d) => d
	}

}
