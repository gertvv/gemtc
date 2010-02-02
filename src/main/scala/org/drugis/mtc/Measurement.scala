package org.drugis.mtc

class Measurement(val treatment: Treatment,
		val responders: Int, val sampleSize: Int) {
	
}

object Measurement {
	def fromXML(node: scala.xml.Node, treatments: Map[String, Treatment])
	: Measurement = {
		val treatment = getTreatment(treatments, (node \ "@treatment").text)
		val responders = (node \ "@responders").text.toInt
		val sample = (node \ "@sample").text.toInt
		new Measurement(treatment, responders, sample)
	}

	private def getTreatment(treatments: Map[String, Treatment], id: String): Treatment = 
		treatments.get(id) match {
			case Some(treatment) => treatment
			case None => throw new IllegalStateException("Non-existent treatment ID referred to!")
		}
}
