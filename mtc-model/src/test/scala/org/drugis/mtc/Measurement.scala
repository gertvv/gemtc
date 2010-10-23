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

		measurement.toXML should be (xml)
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

		measurement.toXML should be (xml)
	}
}
