package org.drugis

class Study(_id: String, treatments: Set[Treatment]) {
	val id = _id
	override def toString = "Study(" + _id + ") = " + treatments

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
}

object Study {
	def fromXML(node: scala.xml.Node,
			treatments: Map[String, Treatment]): Study =
		new Study((node \ "@id").text, treatmentsFromXML(node \ "measurement",
			treatments))

	private def treatmentsFromXML(nodes: scala.xml.NodeSeq,
			treatments: Map[String, Treatment]): Set[Treatment] = 
		Set[Treatment]() ++ 
		{for {node <- nodes} yield getTreatment(treatments, (node \ "@treatment").text)}

	private def getTreatment(treatments: Map[String, Treatment], id: String): Treatment = 
		treatments.get(id) match {
			case Some(treatment) => treatment
			case None => throw new IllegalStateException("Non-existent treatment ID referred to!")
		}
}
