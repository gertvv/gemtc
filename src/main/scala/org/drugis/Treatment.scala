package org.drugis

final class Treatment(_id: String, _desc: String) extends Ordered[Treatment] {
	val id: String = _id
	val description = _desc
	override def toString = "Treatment(" + id + ")"

	def this(_id: String) = this(_id, "")

	def compare(that: Treatment): Int = id.compare(that.id)

	override def equals(other: Any): Boolean =
		other match {
			case that: Treatment =>
				id == that.id
			case _ => false
		}
}

object Treatment {
	def fromXML(node: scala.xml.Node): Treatment =
		new Treatment((node \ "@id").text, node.text)
}
