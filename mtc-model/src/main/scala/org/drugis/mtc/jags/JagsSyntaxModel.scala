/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
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
import java.text.DecimalFormat
import org.mvel2.templates.TemplateRuntime
import org.mvel2.templates.TemplateCompiler
import org.mvel2.templates.CompiledTemplate

import scala.collection.JavaConversions._

class JagsSyntaxModel[M <: Measurement, P <: Parametrization[M]](
		val model: NetworkModel[M, P]
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

	val format = new DecimalFormat("0.0##E0")

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

	private def rewrite(s: String): String = s.replaceFirst("E", "*10^")
	private val varPrior = rewrite(format.format(model.variancePrior))
	private val effPrior = rewrite(format.format(1/model.normalPrior))

	def modelText: String = bugsSyntaxModel
	/*def modelText: String = {
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
	}*/

	def initialValuesText(gen: StartingValueGenerator[M]): String = {
		List(
			initMetaParameters(gen),
			initBaselineEffects(gen),
			initRelativeEffects(gen),
			initVarianceParameters(gen)).mkString("\n")
	}


	def scriptText(prefix: String, chains: Int, tuning: Int, simulation: Int)
	: String = 
		(List(
			"model in '" + prefix + ".model'",
			"data in '" + prefix + ".data'",
			"compile, nchains(" + chains + ")") ++
		{
			for {i <- 1 to chains} yield "parameters in '" + prefix +
				".param" + i + "', chain(" + i + ")"
		} ++
		List(
			"initialize",
			empty,
			"adapt " + tuning,
			empty,
			monitors,
			empty,
			"update " + simulation,
			empty,
			"coda *, stem('" + prefix + "')"
		)).mkString("\n")

	def analysisText(prefix: String): String =
		List(
			"deriv <- list(",
			derivations,
			"\t)",
			"# source('mtc.R')",
			"# data <- append.derived(read.mtc('" + prefix + "'), deriv)"
		).mkString("\n")

	private def monitors =
		(paramNames ++ varNames).map(p => "monitor " + p).mkString("\n")

	private def paramNames = model.parameterVector.map(p => p.toString)
	private def varNames =
		if (inconsistency) {
			List("var.d", "var.w")
		} else {
			List("var.d")
		}

	private val header = "model {"
	private val empty = ""
	private val footer = "}"
	private val baselineEffects = 
	"""	|	# Study baseline effects
		|	for (i in 1:length(b)) {
		|		mu[i] ~ dnorm(0, XXX)
		|	}""".stripMargin.replace("XXX", effPrior)
	private val individualEffects = 
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

	private def deltas: String = 
		(for {
			study <- model.studyList
		} yield studyDeltas(study)).mkString("\n")


	private def studyDeltas(study: Study[M]): String = {
		if (study.treatments.size == 2) twoArmDeltas(study)
		else multiArmDeltas(study)
	}

	private def nonBaselineList(study: Study[M]) = {
		(study.treatments - base(study)).toList.sorted
	}

	private def twoArmDeltas(study: Study[M]) = {
		val treatments = nonBaselineList(study)
		List(
			"\t# Random effects in study " + study.id,
			"\tre[" + idx(study) + ", 1] ~ " +
				normal(express(study, subj(study)), "tau.d"),
			"\t" + zeroDelta(study, base(study)),
			deltasArray(study, treatments, 1)
		).mkString("\n")
	}

	private def subj(study: Study[M]) = {
		require(study.treatments.size == 2)
		(study.treatments - base(study)).toList.first
	}

	private def multiArmDeltas(study: Study[M]) = {
		val treatments = nonBaselineList(study)
		model.splitNode(study) match {
			case None => {
				List(
					"\t# Random effects in study " + study.id,
					paramArray(study, treatments),
					randomEffectArray(study, treatments.size, 1),
					"\t" + zeroDelta(study, base(study)),
					deltasArray(study, treatments, 1)).mkString("\n")
			}
			case Some(p) => {
				val t = p._2;
				{List("\t# Random effects in study " + study.id) ++ {
					if (treatments.size == 2) Nil
					else List(paramArray(study, treatments - t))
				} ++ List(
					"\tre[" + idx(study) + ", 1] ~ " +
						normal(express(study, t), "tau.d"),
					nonSplitEffectArray(study, t),
					"\t" + zeroDelta(study, base(study)),
					"\t" + delta(study, t) + " <- " +
						"re[" + idx(study) + ", 1]",
					deltasArray(study, treatments - t, 2))
				}.mkString("\n")
			}
		}
	}

	private def nonSplitEffectArray(study: Study[M], t: Treatment)
	: String = {
		val treatments = nonBaselineList(study) - t
		if (treatments.size == 1) {
			"\tre[" + idx(study) + ", 2] ~ " +
				normal(express(study, treatments(0)), "tau.d")
		} else {
			randomEffectArray(study, treatments.size, 2)
		}
	}

	private def paramArray(study: Study[M], treatments: List[Treatment]) = 
		(for {
			k <- 1 to treatments.size
		} yield "\td[" + idx(study) + ", " + k + "] <- " +
			express(study, treatments(k - 1))).mkString("\n")

	private def randomEffectArray(study: Study[M], n: Int, n0: Int) = 
		"\tre[" + idx(study) + ", " + n0 + ":" + (n0 + n - 1) + "] ~ " +
			"dmnorm(d[" + idx(study) + ", 1:" + n + "], tau." + n + ")"

	private def deltasArray(study: Study[M], treatments: List[Treatment], k0: Int) = 
		(for {k <- 1 to treatments.size} yield
			"\t" + delta(study, treatments(k - 1)) + " <- " +
			"re[" + idx(study) + ", " + (k + k0 - 1) + "]").mkString("\n")

	private def normal(mean: String, tau: String) =
		"dnorm(" + mean + ", " + tau + ")"

	private def zeroDelta(study: Study[M], subj: Treatment) =
		delta(study, subj) + " <- 0"

	private def delta(study: Study[M], subj: Treatment) = "delta[" +
		idx(study) + ", " + idx(base(study)) + ", " + idx(subj) + "]"

	private def idx(study: Study[M]) = model.studyMap(study)

	private def idx(treatment: Treatment) = model.treatmentMap(treatment)

	private def base(study: Study[M]) = model.studyBaseline(study)

	private def expressParam(p: NetworkModelParameter, v: Int,
		f: String => String): String = 
		v match {
			case  1 => f(p.toString)
			case -1 => "-" + f(p.toString)
			case  _ => throw new Exception("Unexpected value!")
		}

	private def expressParams(params: Map[NetworkModelParameter, Int],
		f: String => String)
	: String =
		(for {(p, v) <- params} yield expressParam(p, v, f)).mkString(" + ")

	private def expressParams(params: Map[NetworkModelParameter, Int])
	: String = expressParams(params, (x) => x)

	def express(study: Study[M], effect: Treatment) = {
		val base = model.studyBaseline(study)
		require(effect != base)
		expressParams(model.parametrization(base, effect))
	}

	private def metaParameters: String =
		(
			List("\t# Meta-parameters") ++
			(for {param <- model.parameterVector} yield format(param))
		).mkString("\n")

	private def asBasic(p: NetworkModelParameter): BasicParameter = p match {
		case b: BasicParameter => b
		case s: SplitParameter => new BasicParameter(s.base, s.subject)
		case _ => throw new IllegalArgumentException("Cannot convert " + p +
			" to a BasicParameter")
	}

	private def initMetaParameters(g: StartingValueGenerator[M]): String = {
		val basic = {
			for {basicParam <- model.basicParameters}
			yield g.getRelativeEffect(asBasic(basicParam))
		}

		
		{
			for {param <- model.parameterVector} yield init(param, g, basic)
		}.mkString("\n")
	}

	private def init(p: NetworkModelParameter, g: StartingValueGenerator[M],
			bl: List[Double])
	: String = "`" + p.toString + "` <-\n" + (p match {
		case b: BasicParameter => bl(model.basicParameters.findIndexOf(_ == b))
		case s: SplitParameter => bl(model.basicParameters.findIndexOf(_ == s))
		case i: InconsistencyParameter =>
			InconsistencyStartingValueGenerator(i, model, g, bl)
		case _ => throw new IllegalStateException("Unsupported parameter " + p)
	})

	private def initBaselineEffects(g: StartingValueGenerator[M]): String = {
		"`mu` <-\nc(" + {
			{
				for {s <- model.studyList} yield g.getBaselineEffect(s)
			}.mkString(",")
		} + ")"
	}

	private def initRelativeEffects(g: StartingValueGenerator[M]): String = {
		val reDim = model.studyList.map(s => s.treatments.size).reduceLeft(Math.max) - 1
		"`re` <-\nstructure(c(" + {
			{
				for {i <- 0 until reDim; s <- model.studyList} yield {
					init(s, i, g)
				}
			}.mkString(",")
		} + "), .Dim = c(" + model.studyList.size + "L," + reDim + "L))"
	}

	private def init(s: Study[M], idx: Int, g: StartingValueGenerator[M])
	: String = {
		if (idx < s.treatments.size - 1) {
			val p = new BasicParameter(base(s), nonBaselineList(s)(idx))
			g.getRandomEffect(s, p).toString
		} else "NA"
	}

	private def initVarianceParameters(g: StartingValueGenerator[M]): String = {
		{
			if (inconsistency) 
				"`sd.w` <-\n" + g.getRandomEffectsVariance() + "\n"
			else ""
		} + "`sd.d` <-\n" + g.getRandomEffectsVariance()
	}

	private def format(p: NetworkModelParameter): String = 
		"\t" + p.toString + " ~ " + normal("0", variance(p))

	private def variance(p: NetworkModelParameter): String = p match {
		case b: BasicParameter => effPrior 
		case s: SplitParameter => effPrior 
		case i: InconsistencyParameter => "tau.w"
		case _ => throw new RuntimeException("Unhandled Parameter type")
	}

	private def basicVar(name: String) = (
		"""	|	sd.x ~ dunif(0, """ + varPrior + """)
			|	var.x <- sd.x * sd.x
			|	tau.x <- 1 / var.x""").stripMargin.replaceAll("x", name)

	private def varDim(s: Study[M]): Int = {
		model.splitNode(s) match {
			case Some(_) => s.treatments.size - 2 
			case None => s.treatments.size - 1
		}
	}

	private def combinedVars: List[String] = 
		for {
			n <- model.network.studies.map(a => varDim(a)).toList;
			if (n > 1)
		} yield "\n" + tauMatrix(n)

	private def tauMatrix(dim: Int)  =
		if (dim == 2)
			"""	|	# 2x2 inv. covariance matrix for 3-arm trials
				|	var.2[1, 1] <- var.d
				|	var.2[1, 2] <- var.d / 2
				|	var.2[2, 1] <- var.d / 2
				|	var.2[2, 2] <- var.d
				|	tau.2 <- inverse(var.2)""".stripMargin
		else throw new Exception("Studies with > 3 arms not supported yet; Please email Gert van Valkenhoef <g.h.m.van.valkenhoef at rug.nl>")

	private def varianceParameters: String =
		if (inconsistency) {
			List(
				inconsistencyVariance,
				empty,
				randomEffectsVariance
			).mkString("\n")
		} else {
			randomEffectsVariance
		}

	private def inconsistencyVariance: String =
		List(
			"\t# Inconsistency variance",
			basicVar("w")).mkString("\n")

	private def randomEffectsVariance: String = 
		(List(
			"\t# Random effect variance",
			basicVar("d")
		) ++ combinedVars).mkString("\n")


	private def derivations = {
		val n = model.treatmentList.size
		val t = model.treatmentList
		(for {i <- 0 until (n - 1); j <- (i + 1) until n
			val p = new BasicParameter(t(i), t(j))
			val p2 = new BasicParameter(t(j), t(i))
			val e = expressParams(model.parametrization(t(i), t(j)),
				(x) => "x[, \"" + x + "\"]")
			if (!model.basicParameters.contains(p) && !model.basicParameters.contains(p2))
		 } yield "\t`" + p + "` = function(x) { " + e + " }").mkString(",\n")
	}

	def relativeEffectMatrix: String = {
		def express(i: Int, j: Int) = {
			val t = model.treatmentList
			if (i == j) "0"
			else expressParams(model.parametrization(t(i), t(j)), (x) => x)
		}
		val n = model.treatmentList.size
		(for {i <- 0 until n; j <- 0 until n}
			yield "\td[" + (i + 1) + "," + (j + 1) + "] <- " + express(i, j)
		).mkString("\n")
	}

	def readTemplate(fileName: String): CompiledTemplate = {
		TemplateCompiler.compileTemplate(getClass().getResourceAsStream(fileName), null)
	}

	def bugsSyntaxModel: String = {
		val template = readTemplate("modelTemplate.txt")
		val map = new java.util.HashMap[String, Object]()
		map.put("dichotomous", dichotomous.asInstanceOf[AnyRef])
		map.put("inconsistency", inconsistency.asInstanceOf[AnyRef])
		map.put("relativeEffectMatrix", relativeEffectMatrix)
		map.put("priorPrecision", effPrior)
		map.put("stdDevUpperLimit", varPrior)
		map.put("parameters", asList(model.parameterVector))
		String.valueOf(TemplateRuntime.execute(template, map))
	}
}

object JagsSyntaxModel {
	/**
	 * Convert a number to a String so that it can be read by S-Plus/R
	 */
	def writeNumber[N <: Number](n: N): String = {
		if (n.isInstanceOf[Int] || n.isInstanceOf[Long]) {
			String.valueOf(n) + "L"
		} else {
			String.valueOf(n)
		}
	}

	/**
	 * Convert a matrix m -- where m(i)(j) is the number in the i-th row and j-th column -- to S-Plus/R format.
	 * @param columnMajor true for column-major format (R/S-Plus/JAGS), false for row-major (BUGS).
	 */
	def writeMatrix[N <: Number](m: List[List[N]], columnMajor: Boolean): String = {
		val rows = m.size
		val cols = m(0).size
		val cells: Seq[String] = {
			if (columnMajor) (0 until cols).map(j => (0 until rows).map(i => writeNumber(m(i)(j)))).flatten
			else m.flatten.map(writeNumber _)
		}
		"structure(c(" + cells.mkString(", ") + "), .Dim = c(" + writeNumber[Integer](rows) + ", " + writeNumber[Integer](cols) + "))"
	}
}
