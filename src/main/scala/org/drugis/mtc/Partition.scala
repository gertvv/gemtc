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

	private def allStudiesHave(t: Treatment) =
		studies.forall(s => s.treatments.contains(t))
}

/**
 * Represents a valid partition.
 * The set of parts is: {{x, x} -> P} OR {{x, y} -> P, {x, y} -> Q} OR
 * the parts have non-equal treatment sets and form a cycle.
 */
class Partition[M <: Measurement](val parts: Set[Part[M]]) {
	def reduce: Partition[M] = null
	override def equals(other: Any) = false
	override def hashCode: Int = 0
}

object Partition {
	def apply[M <: Measurement](network: Network[M], cycle: Cycle[Treatment]) 
	:Partition[M] = null
}
