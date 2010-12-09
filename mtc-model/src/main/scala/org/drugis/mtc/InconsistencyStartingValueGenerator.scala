package org.drugis.mtc

object InconsistencyStartingValueGenerator {
	/**
	 * Derive a starting value for the InconsistencyParameter based on the
	 * values assigned to the BasicParameters.
	 */
	def apply[M <: Measurement, P <: Parametrization[M]](
		p: InconsistencyParameter, proto: NetworkModel[M, P],
		startVal: StartingValueGenerator[M], basicStart: List[Double])
	: Double = {
		val e = proto.parametrization.basis.backEdges.find(
			e => occursIn(p.cycle, e)) match {
			case Some(x) => x
			case _ => throw new IllegalStateException("Cycle not found")
		}
		val fp = new BasicParameter(e._1, e._2)
		val pmtz = inconsistencyEquation(
			proto.parametrization(e._1, e._2), fp)
		pmtz.map(x => x._2 * {
				if (x._1 == fp) startVal.getRelativeEffect(fp)
				else basicStart(proto.basicParameters.findIndexOf(q => q == x._1))
			}).reduceLeft((a, b) => a + b)
	}

	def inconsistencyEquation(
		pmtz: Map[NetworkModelParameter, Int],
		fp: NetworkModelParameter)
	: Map[NetworkModelParameter, Int] = {
		val inc = pmtz.find(_._1 match {
			case w: InconsistencyParameter => true 
			case _ => false
		}) match {
			case Some(x) => x
			case None => throw new IllegalStateException("No inconsistency found")
		}
		val w = inc._1
		val f = inc._2
		(pmtz - w).mapValues(v => v * -1 * f) + ((fp, f))
	}

	/**
	 * Check if edge e occurs in the cycle c
	 */
	def occursIn(c: List[Treatment], e: (Treatment, Treatment)) = {
		c.containsSlice(List(e._1, e._2)) || c.containsSlice(List(e._2, e._1))
	}
}
