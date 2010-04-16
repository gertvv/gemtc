package org.drugis.mtc

import scala.collection.mutable.{Map => MMap}
import org.drugis.mtc.{DichotomousMeasurement => M}

class NetworkBuilder {
	private val measurementMap = MMap[(String, Treatment), M]()
	private val treatmentMap = MMap[String, Treatment]()

	def add(studyId: String, treatmentId: String,
			responders: Int, sampleSize: Int) {
		val m = createMeasurement(treatmentId, responders, sampleSize)
		put((studyId, m.treatment), m)
	}

	def buildNetwork(): Network = new Network(treatmentSet, studySet)

	def getTreatment(tId: String): Treatment = treatmentMap(tId)

	private def createMeasurement(tId: String, r: Int, n: Int): M = {
		val t: Treatment = treatmentMap.get(tId) match {
			case None => addTreatment(tId)
			case Some(x: Treatment) => x
		}

		new DichotomousMeasurement(t, r, n)
	}

	private def addTreatment(id: String): Treatment = {
		val t = new Treatment(id)
		treatmentMap.put(id, t)
		t
	}

	private def put(k: (String, Treatment), v: M) {
		if (measurementMap.contains(k)) {
			throw new IllegalArgumentException("Study/Treatment combination " +
				"already mapped.");
		}
		measurementMap.put(k, v)
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
