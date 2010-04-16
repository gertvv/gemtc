package org.drugis.mtc

class Measurement(val treatment: Treatment, val sampleSize: Int) {
}

class DichotomousMeasurement(treatment: Treatment,
		val responders: Int, sampleSize: Int) 
extends Measurement(treatment, sampleSize) {
}

class ContinuousMeasurement(treatment: Treatment,
		val mean: Double, val stdDev: Double, sampleSize: Int)
extends Measurement(treatment, sampleSize) {
	val stdErr = stdDev / Math.sqrt(sampleSize.toDouble)
}

trait MeasurementBuilder {
	protected def getTreatment(treatments: Map[String, Treatment], id: String): Treatment = 
		treatments.get(id) match {
			case Some(treatment) => treatment
			case None => throw new IllegalStateException("Non-existent treatment ID referred to!")
		}
}

object DichotomousMeasurement extends MeasurementBuilder {
	def fromXML(node: scala.xml.Node, treatments: Map[String, Treatment])
	: DichotomousMeasurement = {
		val treatment = getTreatment(treatments, (node \ "@treatment").text)
		val responders = (node \ "@responders").text.toInt
		val sample = (node \ "@sample").text.toInt
		new DichotomousMeasurement(treatment, responders, sample)
	}
}

object ContinuousMeasurement extends MeasurementBuilder {
	def fromXML(node: scala.xml.Node, treatments: Map[String, Treatment])
	: ContinuousMeasurement = {
		val treatment = getTreatment(treatments, (node \ "@treatment").text)
		val mean = (node \ "@mean").text.toDouble
		val stdDev = (node \ "@standardDeviation").text.toDouble
		val sample = (node \ "@sample").text.toInt
		new ContinuousMeasurement(treatment, mean, stdDev, sample)
	}
}
