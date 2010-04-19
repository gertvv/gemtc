package org.drugis.mtc

final class Study[M <: Measurement](val id: String,
		val measurements: Map[Treatment, M]) {
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
		case that: Study[M] =>
			that.id == this.id && that.treatments == this.treatments
		case _ => false
	}

	override def hashCode = id.hashCode
}

object Study {
	def fromXML[M <: Measurement](node: scala.xml.Node,
			treatments: Map[String, Treatment],
			measReader: (scala.xml.Node, Map[String, Treatment]) => M)
	: Study[M] =
		new Study[M]((node \ "@id").text,
			measurementsFromXML(node \ "measurement", treatments, measReader))

	private def measurementsFromXML[M <: Measurement](
			nodes: scala.xml.NodeSeq,
			treatments: Map[String, Treatment],
			reader: (scala.xml.Node, Map[String, Treatment]) => M)
	: Map[Treatment, M] =
		Map[Treatment, M]() ++
		{for {node <- nodes; val m = reader(node, treatments)
		} yield (m.treatment, m)}

	private def measurementMap[M <: Measurement](ms: Array[M]) =
		Map[Treatment, M]() ++ ms.map(m => (m.treatment, m))

	def build[M <: Measurement](id: String, measurements: Array[M])
	: Study[M] = {
		new Study[M](id, measurementMap(measurements));
	}

	def buildDichotomous(id: String,
			measurements: Array[DichotomousMeasurement])
	: Study[DichotomousMeasurement] = {
		build(id, measurements)
	}

	def buildContinuous(id: String,
			measurements: Array[ContinuousMeasurement])
	: Study[ContinuousMeasurement] = {
		build(id, measurements)
	}
}
