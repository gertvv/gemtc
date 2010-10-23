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

	def toXML = <study id={id}>{measurements.values.toList.sort((a, b) => a.treatment.id < b.treatment.id).map(m => m.toXML)}</study>
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
