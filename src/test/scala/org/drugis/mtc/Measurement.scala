package org.drugis.mtc

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

class MeasurementTest extends ShouldMatchersForJUnit {
	@Test def testFromXML() {
		val treatmentA = new Treatment("A")
		val treatmentB = new Treatment("B")
		val treatments = Map[String, Treatment](
			("A", treatmentA), ("B", treatmentB))
		val xml = <measurement treatment="A" responders="9" sample="140" />

		val measurement = DichotomousMeasurement.fromXML(xml, treatments)
		measurement.treatment should be (treatmentA)
		measurement.responders should be (9)
		measurement.sampleSize should be (140)
	}

	@Test def testFromXML2() {
		val treatments = Map[String, Treatment](("B", new Treatment("B")))
		val xml = <measurement treatment="A" responders="9" sample="140" />
		intercept[IllegalStateException] {
			DichotomousMeasurement.fromXML(xml, treatments)
		}
	}

	@Test def testFromXMLContinuous() {
		val treatmentA = new Treatment("A")
		val treatmentB = new Treatment("B")
		val treatments = Map[String, Treatment](
			("A", treatmentA), ("B", treatmentB))
		val xml = <measurement treatment="A" mean="1.3" standardDeviation="0.3" sample="140" />

		val measurement = ContinuousMeasurement.fromXML(xml, treatments)
		measurement.treatment should be (treatmentA)
		measurement.mean should be (1.3)
		measurement.stdDev should be (0.3)
		measurement.sampleSize should be (140)
		measurement.stdErr should be (0.3/Math.sqrt(140.toDouble))
	}
}
