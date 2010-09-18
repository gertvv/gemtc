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

package org.drugis.mtc.jags

import org.drugis.mtc._

abstract class JagsSyntaxModel[M <: Measurement, P <: Parametrization[M]](
		model: NetworkModel[M, P]
) {
	val dichotomous: Boolean = {
		val cls = model.network.measurementType
		if (cls == classOf[DichotomousMeasurement])
			true
		else if (cls == classOf[ContinuousMeasurement])
			false
		else
			throw new IllegalStateException("Unknown measurement type " + cls)
	}

	val inconsistency: Boolean = model.parametrization match {
		case i: InconsistencyParametrization[M] => true
		case _ => false
	}

	def dataText: String =
		if (dichotomous) {
			List(
				vectorStr("s", studyIndexVector),
				vectorStr("t", treatmentIndexVector),
				vectorStr("r", responderVector),
				vectorStr("n", sampleSizeVector),
				vectorStr("b", baselineVector)
			).mkString("\n")
		} else {
			List(
				vectorStr("s", studyIndexVector),
				vectorStr("t", treatmentIndexVector),
				vectorStr("m", meanVector),
				vectorStr("e", errorVector),
				vectorStr("b", baselineVector)
			).mkString("\n")
		}

	private def studyIndexVector: List[Int] =
		model.data.map(a => model.studyMap(a._1))

	private def treatmentIndexVector: List[Int] =
		model.data.map(a => model.treatmentMap(a._2.treatment))

	private def meanVector: List[Double] =
		model.data.map(a => a._2.asInstanceOf[ContinuousMeasurement].mean)

	private def errorVector: List[Double] =
		model.data.map(a => a._2.asInstanceOf[ContinuousMeasurement].stdErr)

	private def responderVector: List[Int] = 
		model.data.map(a => a._2.asInstanceOf[DichotomousMeasurement].responders)

	private def sampleSizeVector: List[Int] =
		model.data.map(a => a._2.sampleSize)

	private def baselineVector: List[Int] = 
		model.studyList.map(s => model.treatmentMap(model.studyBaseline(s)))

	private def vectorStr(name: String, vector: List[_]): String = {
		name + " <- c(" + vector.mkString(", ") + ")"
	}

	def modelText: String
	def scriptText(prefix: String): String
	def analysisText(prefix: String): String
	def expressParams(params: Map[NetworkModelParameter, Int]): String
}

class JagsSyntaxConsistencyModel[M <: Measurement, P <: Parametrization[M]](
	model: NetworkModel[M, P])
extends JagsSyntaxInconsistencyModel(model) {
}

class JagsSyntaxInconsistencyModel[M <: Measurement, P <: Parametrization[M]](
	model: NetworkModel[M, P])
extends JagsSyntaxModel(model) {
	override def modelText: String = {
		List(
			header,
			baselineEffects,
			empty,
			deltas,
			empty,
			individualEffects,
			empty,
			metaParameters,
			empty,
			varianceParameters,
			footer).mkString("\n")
	}

	override def scriptText(prefix: String): String = 
		List(
			"model in '" + prefix + ".model'",
			"data in '" + prefix + ".data'",
			"compile",
			"initialize",
			empty,
			"update 30000",
			empty,
			monitors,
			empty,
			"update 20000",
			empty,
			"monitors to '" + prefix + ".R'"
		).mkString("\n")

	override def analysisText(prefix: String): String =
		List(
			"source('" + prefix + ".R')",
			"attach(trace)",
			"data <- list()",
			standardizeParameters("data"),
			standardizeVariances("data"),
			"detach(trace)"
		).mkString("\n")

	protected def monitors =
		(paramNames ++ varNames).map(p => "monitor " + p).mkString("\n")

	private def paramNames = model.parameterVector.map(p => p.toString)
	private def varNames =
		if (inconsistency) {
			List("var.d", "var.w")
		} else {
			List("var.d")
		}

	protected val header = "model {"
	protected val empty = ""
	protected val footer = "}"
	protected val baselineEffects = 
	"""	|	# Study baseline effects
		|	for (i in 1:length(b)) {
		|		mu[i] ~ dnorm(0, .001)
		|	}""".stripMargin
	protected val individualEffects = 
		if (dichotomous)
	"""	|	# For each (study, treatment), model effect
		|	for (i in 1:length(s)) {
		|		logit(p[s[i], t[i]]) <- mu[s[i]] + delta[s[i], b[s[i]], t[i]]
		|		r[i] ~ dbin(p[s[i], t[i]], n[i])
		|	}""".stripMargin
		else
	"""	|	# For each (study, treatment), model effect
		|	for (i in 1:length(s)) {
		|		p[s[i], t[i]] <- mu[s[i]] + delta[s[i], b[s[i]], t[i]]
		|		m[i] ~ dnorm(p[s[i], t[i]], 1 / (e[i] * e[i]))
		|	}""".stripMargin
	protected val varPrior = model.variancePrior

	protected def deltas: String = 
		(for {
			study <- model.studyList
		} yield studyDeltas(study)).mkString("\n")

	private def studyDeltas(study: Study[M]): String = 
		if (study.treatments.size == 2) twoArmDeltas(study)
		else multiArmDeltas(study)

	private def twoArmDeltas(study: Study[M]) =
		List(
			"\t# Random effects in study " + study.id,
			"\t" + zeroDelta(study, base(study)),
			"\t" + delta(study, subj(study)) + " ~ " +
				normal(express(study, subj(study)), "tau.d")
		).mkString("\n")

	private def subj(study: Study[M]) = {
		require(study.treatments.size == 2)
		(study.treatments - base(study)).toList.first
	}

	private def multiArmDeltas(study: Study[M]) = {
		val treatments = (study.treatments - base(study)).toList
		List(
			"\t# Random effects in study " + study.id,
			paramArray(study, treatments),
			randomEffectArray(study, treatments.size),
			"\t" + zeroDelta(study, base(study)),
			deltasArray(study, treatments)).mkString("\n")
	}

	private def paramArray(study: Study[M], treatments: List[Treatment]) = 
		(for {
			k <- 1 to treatments.size
		} yield "\td[" + idx(study) + ", " + k + "] <- " +
			express(study, treatments(k - 1))).mkString("\n")

	private def randomEffectArray(study: Study[M], n: Int) = 
		"\tre[" + idx(study) + ", 1:" + n + "] ~ " +
			"dmnorm(d[" + idx(study) + ", 1:" + n + "], tau." + n + ")"

	private def deltasArray(study: Study[M], treatments: List[Treatment]) = 
		(for {k <- 1 to treatments.size} yield
			"\t" + delta(study, treatments(k - 1)) + " <- " +
			"re[" + idx(study) + ", " + k + "]").mkString("\n")

	private def normal(mean: String, tau: String) =
		"dnorm(" + mean + ", " + tau + ")"

	private def zeroDelta(study: Study[M], subj: Treatment) =
		delta(study, subj) + " <- 0"

	private def delta(study: Study[M], subj: Treatment) = "delta[" +
		idx(study) + ", " + idx(base(study)) + ", " + idx(subj) + "]"

	private def idx(study: Study[M]) = model.studyMap(study)

	private def idx(treatment: Treatment) = model.treatmentMap(treatment)

	private def base(study: Study[M]) = model.studyBaseline(study)

	protected def expressParam(p: NetworkModelParameter, v: Int): String = 
		v match {
			case  1 => p.toString
			case -1 => "-" + p.toString
			case  _ => throw new Exception("Unexpected value!")
		}

	override def expressParams(params: Map[NetworkModelParameter, Int])
	: String =
		(for {(p, v) <- params} yield expressParam(p, v)).mkString(" + ")

	def express(study: Study[M], effect: Treatment) = {
		val base = model.studyBaseline(study)
		require(effect != base)
		expressParams(model.parametrization(base, effect))
	}

	protected def metaParameters: String =
		(
			List("\t# Meta-parameters") ++
			(for {param <- model.parameterVector} yield format(param))
		).mkString("\n")

	private def format(p: NetworkModelParameter): String = 
		"\t" + p.toString + " ~ " + normal("0", variance(p))

	private def variance(p: NetworkModelParameter): String = p match {
		case b: BasicParameter => ".001"
		case s: SplitParameter => ".001"
		case i: InconsistencyParameter => "tau.w"
		case _ => throw new RuntimeException("Unhandled Parameter type")
	}

	private def basicVar(name: String) = (
		"""	|	sd.x ~ dunif(0.00001, """ + varPrior + """)
			|	var.x <- sd.x * sd.x
			|	tau.x <- 1 / var.x""").stripMargin.replaceAll("x", name)

	private def combinedVars: List[String] = 
		for {
			n <- model.network.studies.map(a => a.treatments.size).toList;
			if (n > 2)
		} yield "\n" + tauMatrix(n - 1)

	private def tauMatrix(dim: Int)  =
		if (dim == 2)
			"""	|	# 2x2 inv. covariance matrix for 3-arm trials
				|	var.2[1, 1] <- var.d
				|	var.2[1, 2] <- var.d / 2
				|	var.2[2, 1] <- var.d / 2
				|	var.2[2, 2] <- var.d
				|	tau.2 <- inverse(var.2)""".stripMargin
		else throw new Exception("Studies with > 3 arms not supported yet; Please email Gert van Valkenhoef <g.h.m.van.valkenhoef at rug.nl>")

	protected def varianceParameters: String =
		if (inconsistency) {
			List(
				inconsistencyVariance,
				empty,
				randomEffectsVariance
			).mkString("\n")
		} else {
			randomEffectsVariance
		}

	protected def inconsistencyVariance: String =
		List(
			"\t# Inconsistency variance",
			basicVar("w")).mkString("\n")

	protected def randomEffectsVariance: String = 
		(List(
			"\t# Random effect variance",
			basicVar("d")
		) ++ combinedVars).mkString("\n")


	protected def standardizeParameters(frame: String) =
		(standardParameters(frame) ++ standardInconsistencies(frame)
		).mkString("\n")

	protected def standardParameters(frame: String) = 
		(for {edge <- model.network.edgeVector
			val p = new BasicParameter(edge._1, edge._2)
			val e = expressParams(model.parametrization(edge._1, edge._2))
		 } yield frame + "$" + p + " <- " + e)

	private def standardInconsistencies(frame: String) = 
		(for {param <- model.parameterVector;
			if (param.isInstanceOf[InconsistencyParameter])
		} yield frame + "$" + param + " <- " + param)
			

	protected def standardizeVariances(frame: String) = 
		if (inconsistency)
			List(
				frame + "$var.d <- var.d",
				frame + "$var.w <- var.w"
			).mkString("\n")
		else
			frame + "$var.d <- var.d"
}
