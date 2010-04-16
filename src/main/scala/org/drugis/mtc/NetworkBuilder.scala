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
