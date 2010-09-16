/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2010 Gert van Valkenhoef.
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

package org.drugis.mtc

abstract class Parametrization[M <: Measurement](
		val network: Network[M],
		val basis: FundamentalGraphBasis[Treatment]
) {
	val cycles: Set[Cycle[Treatment]] = 
		basis.cycles.map(c => Cycle(c))

	def basicParameters: List[BasicParameter] =
		sort(basis.treeEdges).map(e => new BasicParameter(e._1, e._2))

	private def sort(edges: Set[(Treatment, Treatment)])
	: List[(Treatment, Treatment)] =
		edges.toList.sort(
			(a, b) => if (a._1 == b._1) a._2 < b._2 else a._1 < b._1)

	/**
	 * Parametrization of d.t1.t2 (effect of t2 relative to t1).
	 */
	def apply(t1: Treatment, t2: Treatment): Map[NetworkModelParameter, Int] =
		param(t1, t2, true)

	protected def param(a: Treatment, b: Treatment, direct: Boolean)
	: Map[NetworkModelParameter, Int] = {
		if (basis.treeEdges contains (a, b))
			basicParam(a, b, direct)
		else if (basis.treeEdges contains (b, a))
			negate(basicParam(b, a, direct))
		else
			functionalParam(a, b)
	}

	protected def basicParam(a: Treatment, b: Treatment, direct: Boolean) =
		Map[NetworkModelParameter, Int]((new BasicParameter(a, b), 1))
	
	protected def functionalParam(a: Treatment, b: Treatment)
	: Map[NetworkModelParameter, Int] = {
		val r = basis.tree.commonAncestor(a, b)
		add(negate(pathParam(basis.tree.path(r, a))),
			pathParam(basis.tree.path(r, b)))
	}

	private def pathParam(path: List[Treatment])
	: Map[NetworkModelParameter, Int] = {
		if (path.size < 2) emptyParam()
		else add(param(path(0), path(1), false), pathParam(path.tail))
	}

	protected def negate(p: Map[NetworkModelParameter, Int]) =
		p.transform((a, b) => -b)

	protected def add(p: Map[NetworkModelParameter, Int],
			q: Map[NetworkModelParameter, Int])
	: Map[NetworkModelParameter, Int] = {
		emptyParam() ++
		(for {x <- (p.keySet ++ q.keySet)
		} yield (x, getOrZero(p, x) + getOrZero(q, x)))
	}

	protected def emptyParam() = Map[NetworkModelParameter, Int]()

	protected def getOrZero(p: Map[NetworkModelParameter, Int],
			x: NetworkModelParameter): Int =
	p.get(x) match {
		case None => 0
		case Some(d) => d
	}
}

class ConsistencyParametrization[M <: Measurement](
		network: Network[M],
		basis: FundamentalGraphBasis[Treatment]
) extends Parametrization[M](network, basis) {
}

class InconsistencyParametrization[M <: Measurement] (
		network: Network[M],
		basis: FundamentalGraphBasis[Treatment]
) extends Parametrization[M](network, basis) {
	val cycleClass: Map[Cycle[Treatment], Option[(Partition[M], Int)]] = 
		Map[Cycle[Treatment], Option[(Partition[M], Int)]]() ++
			cycles.map(c => classEntry(c))

	val inconsistencyCycles: Map[Partition[M], Set[Cycle[Treatment]]] =
		mapInconsistencyCycles(cycleClass.keySet.toList)

	def inconsistencyClasses: Set[Partition[M]] =
		Set[Partition[M]]() ++ inconsistencyCycles.keySet

	def inconsistencyDegree: Int = inconsistencyClasses.size

	def inconsistencyParameters: List[InconsistencyParameter] = 
		cycleSort(inconsistencyClasses.map(asCycle _)).map(c =>
			new InconsistencyParameter(c.vertexSeq))

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

	override protected def functionalParam(a: Treatment, b: Treatment)
	: Map[NetworkModelParameter, Int] = {
		add(super.functionalParam(a, b), inconsParam(a, b))
	}

	private def inconsParam(a: Treatment, b: Treatment)
	: Map[NetworkModelParameter, Int] = {
		if (a < b) inconsParam(Cycle(basis.tree.cycle(a, b)))
		else negate(inconsParam(Cycle(basis.tree.cycle(b, a))))
	}

	private def inconsParam(cycle: Cycle[Treatment])
	: Map[NetworkModelParameter, Int] = {
		val clsopt = try {
			cycleClass(cycle)
		} catch {
			case e: NoSuchElementException => None
		}
	 	clsopt match {
			case None => emptyParam()
			case Some(cls) => Map[NetworkModelParameter, Int](
				(new InconsistencyParameter(asCycle(cls._1).vertexSeq), cls._2))
		}
	}

}
