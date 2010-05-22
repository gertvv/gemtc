package org.drugis.mtc

class Part[M <: Measurement](t1: Treatment, t2: Treatment,
		val studies: Set[Study[M]]) {
	require(!studies.isEmpty)
	require(allStudiesHave(t1))
	require(allStudiesHave(t2))

	val treatments = Set(t1, t2)

	override def equals(other: Any): Boolean = other match {
		case that: Part[M] => {
			that.treatments == treatments && that.studies == studies }
		case _ => false
	}

	override def hashCode: Int = 31 * treatments.hashCode + studies.hashCode

	override def toString: String =
		"Part({" + treatments.mkString(", ") + "} -> {" +
			studies.map(s => s.id).mkString(", ") + "})"

	private def allStudiesHave(t: Treatment) =
		studies.forall(s => s.treatments.contains(t))
}

/**
 * Represents a valid partition.
 * The set of parts is: {{x, x} -> P} OR {{x, y} -> P, {x, y} -> Q} OR
 * the parts have non-equal treatment sets and form a cycle.
 */
class Partition[M <: Measurement](val parts: Set[Part[M]]) {
	require(validPartition)

	def reduce: Partition[M] =
		if (parts.size < 3) this
		else new Partition(reduce(Cycle(asGraph).edgeSeq))

	private def support(e: (Treatment, Treatment)): Set[Study[M]] =
		parts.find(p => p.treatments == Set(e._1, e._2)) match {
			case Some(p) => p.studies
			case None => throw new IllegalStateException()
		}

	private def reduce(l: List[(Treatment, Treatment)]): Set[Part[M]] = {
		val e = l.head
		val r = reduce(l, e._1, e._2, support(e))
		if (support(l.head) == support(l.last) && r.size > 1) {
			mergeOnVertex(r, e._1)
		} else r
	}

	private def mergeOnVertex(r: Set[Part[M]], v: Treatment) = {
		def other(p: Part[M]) = (p.treatments - v).toList(0)
		val ps = r.filter(p => p.treatments.contains(v)).toList
		require(ps.size == 2)
		require(ps(0).studies == ps(1).studies)
		val merged = new Part(other(ps(0)), other(ps(1)), ps(0).studies)
		(r -- ps) + merged
	}

	private def reduce(l: List[(Treatment, Treatment)], t0: Treatment,
		t1: Treatment, s: Set[Study[M]])
	: Set[Part[M]] = l match {
		case Nil => Set(new Part(t0, t1, s))
		case e :: l1 =>
			if (support(e) == s) reduce(l1, t0, e._2, s)
			else Set(new Part(t0, t1, s)) ++ reduce(l1, e._1, e._2, support(e))
	}

	override def equals(other: Any) = other match {
		case that: Partition[M] => that.parts == parts
		case _ => false
	}

	override def hashCode: Int = parts.hashCode

	override def toString: String = "Partition(" + parts.mkString(", ") + ")"

	private def validPartition: Boolean = {
		val partList = parts.toList
		if (parts.size == 0) false
		else if (parts.size == 1) partList(0).treatments.size == 1
		else if (parts.size == 2)
			partList(0).treatments == partList(1).treatments
		else try { Cycle(asGraph); true } catch { case _ => false }
	}

	private def asGraph = new UndirectedGraph(parts.map(asEdge _))

	private def asEdge(p: Part[M]) = {
		val t = p.treatments.toList
		(t(0), t(1))
	}
}

object Partition {
	def apply[M <: Measurement](network: Network[M], cycle: Cycle[Treatment]) 
	:Partition[M] = null
}
