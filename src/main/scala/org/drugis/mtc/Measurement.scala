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

class Measurement(val treatment: Treatment, val sampleSize: Int) {
}

class NoneMeasurement(treatment: Treatment)
extends Measurement(treatment, 0) {
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

object NoneMeasurement extends MeasurementBuilder {
	def fromXML(node: scala.xml.Node, treatments: Map[String, Treatment])
	: NoneMeasurement = {
		val treatment = getTreatment(treatments, (node \ "@treatment").text)
		new NoneMeasurement(treatment)
	}
}
