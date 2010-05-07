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

import scala.collection.mutable.{Map => MMap}

trait NetworkBuilder[M <: Measurement] {
	private val measurementMap = MMap[(String, Treatment), M]()
	private val treatmentMap = MMap[String, Treatment]()

	def buildNetwork(): Network[M] = new Network[M](treatmentSet, studySet)

	def getTreatment(tId: String): Treatment = treatmentMap(tId)

	private def addTreatment(id: String): Treatment = {
		val t = new Treatment(id)
		treatmentMap.put(id, t)
		t
	}

	protected def put(k: (String, Treatment), v: M) {
		if (measurementMap.contains(k)) {
			throw new IllegalArgumentException("Study/Treatment combination " +
				"already mapped.");
		}
		measurementMap.put(k, v)
	}

	protected def makeTreatment(tId: String): Treatment = 
		treatmentMap.get(tId) match {
			case None => addTreatment(tId)
			case Some(x: Treatment) => x
		}

	private def treatmentSet: Set[Treatment] =
		Set[Treatment]() ++ treatmentMap.values

	private def studySet: Set[Study[M]] = {
		val idSet = measurementMap.keySet.map(x => x._1)
		return Set[Study[M]]() ++ idSet.map(id => study(id))
	}

	private def study(id: String): Study[M] = {
		val measurements = measurementMap.keySet.filter(x => x._1 == id).map(
			k => measurementMap(k))
		new Study[M](id, Map[Treatment, M]() ++
			measurements.map(m => (m.treatment, m)))
	}
}

class DichotomousNetworkBuilder extends NetworkBuilder[DichotomousMeasurement] {
	def add(studyId: String, treatmentId: String,
			responders: Int, sampleSize: Int) {
		val m = createMeasurement(treatmentId, responders, sampleSize)
		put((studyId, m.treatment), m)
	}

	private def createMeasurement(tId: String, r: Int, n: Int)
	: DichotomousMeasurement = {

		new DichotomousMeasurement(makeTreatment(tId), r, n)
	}
}

class ContinuousNetworkBuilder extends NetworkBuilder[ContinuousMeasurement] {
	def add(studyId: String, treatmentId: String,
			mean: Double, stdDev: Double, sampleSize: Int) {
		val m = createMeasurement(treatmentId, mean, stdDev, sampleSize)
		put((studyId, m.treatment), m)
	}

	private def createMeasurement(tId: String, m: Double, s: Double, n: Int)
	: ContinuousMeasurement = {
		new ContinuousMeasurement(makeTreatment(tId), m, s, n)
	}
}
