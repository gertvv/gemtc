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
