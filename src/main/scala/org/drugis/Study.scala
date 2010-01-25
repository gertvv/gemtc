package org.drugis

final class Study(val id: String,
		val measurements: Map[Treatment, Measurement]) {
	val treatments = Set[Treatment]() ++ measurements.keySet
	override def toString = "Study(" + id + ") = " + treatments

	def treatmentGraph: UndirectedGraph[Treatment] = {
		val tList = treatments.toList.sort((a, b) => (a < b))
		var edgeSet = Set[(Treatment, Treatment)]()
		for (i <- 0 to tList.size - 2) {
			for (j <- (i + 1) to tList.size - 1) {
				val edge: (Treatment, Treatment) = (tList(i), tList(j))
				edgeSet = edgeSet + edge
			}
		}
		new UndirectedGraph[Treatment](edgeSet)
	}

	override def equals(other: Any) = other match {
		case that: Study =>
			that.id == this.id && that.treatments == this.treatments
		case _ => false
	}

	override def hashCode = id.hashCode
}

object Study {
	def fromXML(node: scala.xml.Node,
			treatments: Map[String, Treatment]): Study =
		new Study((node \ "@id").text, measurementsFromXML(node \ "measurement",
			treatments))

	private def measurementsFromXML(nodes: scala.xml.NodeSeq,
			treatments: Map[String, Treatment]): Map[Treatment, Measurement] =
		Map[Treatment, Measurement]() ++
		{for {node <- nodes; val m = Measurement.fromXML(node, treatments)} yield (m.treatment, m)}

	private def treatmentsFromXML(nodes: scala.xml.NodeSeq,
			treatments: Map[String, Treatment]): Set[Treatment] = 
		Set[Treatment]() ++ measurementsFromXML(nodes, treatments).keySet

	private def getTreatment(treatments: Map[String, Treatment], id: String): Treatment = 
		treatments.get(id) match {
			case Some(treatment) => treatment
			case None => throw new IllegalStateException("Non-existent treatment ID referred to!")
		}
}
