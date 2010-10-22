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

package org.drugis.mtc.yadas

import scala.collection.mutable.ArrayBuffer
import org.drugis.mtc.Parameter
import org.drugis.mtc.MCMCResults
import gov.lanl.yadas.MCMCParameter

abstract class ParameterWriter(val p: MCMCParameter, val i: Int) {
	def output() {
		write(p.getValue(i))
	}

	def write(x: Double): Unit
}

class Derivation(val pmtz: Map[Parameter, Int]) {
	def calculate(results: MCMCResults, c: Int): Seq[Double] = {
		(0 until results.getNumberOfSamples).map(i => calculate(results, c, i))
	}
	def calculate(results: MCMCResults, c: Int, i: Int): Double = {
		pmtz.keySet.map(p => pmtz(p) * results.getSample(results.findParameter(p), c, i)).reduceLeft((a, b) => a + b)
	}
}

class RandomEffectsVariance extends Parameter {
	override def getName = "var.d"
}

class InconsistencyVariance extends Parameter {
	override def getName = "var.w"
}

class YadasResults extends MCMCResults {
	private var results: List[List[ArrayBuffer[Double]]] = List()
	private var directParameters: List[Parameter] = List()
	private var derivedParameters: List[Parameter] = List()
	private var derivations: List[Derivation] = List()
	private var nChains: Int = 0
	private var nSamples: Int = 0

	private class YadasParameterWriter(
		val paramIdx: Int, val chainIdx: Int, mp: MCMCParameter, i: Int)
	extends ParameterWriter(mp, i) {
		private var idx: Int = 0

		override def write(x: Double): Unit = {
			results(chainIdx)(paramIdx)(idx) = x
			idx += 1
		}
	}

	def setDirectParameters(p: List[Parameter]): Unit = {
		directParameters = p
		initResults()
	}

	def setDerivedParameters(p: List[(Parameter, Derivation)]): Unit = {
		derivedParameters = p.map(x => x._1)
		derivations = p.map(x => x._2)
	}

	def setNumberOfChains(n: Int): Unit = {
		nChains = n
		initResults()
	}

	def setNumberOfIterations(n: Int): Unit = {
		nSamples = n
		results = results.map(chain => chain.map(param => param.padTo(n, 0.0)))
	}

	def initResults(): Unit = {
		results = (0 until nChains).map(x => newChain).toList
	}

	def newChain: List[ArrayBuffer[Double]] = {
		directParameters.map(x => new ArrayBuffer[Double](nSamples).padTo(nSamples, 0.0))
	}

	/**
	 * Writer to write samples from mp[i] to parameter p, chain c.
	 */
	def getParameterWriter(p: Parameter, c: Int, mp: MCMCParameter, i: Int)
	: ParameterWriter = new YadasParameterWriter(findParameter(p), c, mp, i)

	def getParameters: Array[Parameter] = directParameters.toArray
	def findParameter(p: Parameter): Int = 
		(directParameters ++ derivedParameters).findIndexOf(x => x == p)
	def getNumberOfChains: Int = nChains
	def getNumberOfSamples: Int = nSamples
	def getSample(p: Int, c: Int, i: Int): Double = results(c)(p)(i)
	def getSamples(p: Int, c: Int): Array[Double] =
		if (p < directParameters.size) {
			results(c)(p).toArray
		} else {
			derivations(p - directParameters.size).calculate(this, c).toArray
		}
}
