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
		val model: NetworkModel[M, P],
		val isJags: Boolean
) {
	def this(model: NetworkModel[M, P]) = this(model, true)

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

	private def rewrite(s: String): String = 
		if (isJags) s.replaceFirst("E", "*10^")
		else s
	private val varPrior = rewrite(format.format(model.variancePrior))
	private val effPrior = rewrite(format.format(1/model.normalPrior))

	def generateDataFile(lines: List[(String, String)]): String = {
		val assign = { if (isJags) " <- " else " = " }
		val sep = { if (isJags) "\n" else ",\n" }
		val head = { if (isJags) "" else "list(\n" }
		val foot = { if (isJags) "\n" else "\n)\n" }

		head + lines.map(x => x._1 + assign + x._2).mkString(sep) + foot
	}

	def initialValuesText(gen: StartingValueGenerator[M]): String = 
		generateDataFile(
			initMetaParameters(gen) :::
			initBaselineEffects(gen) ::
			initRelativeEffects(gen) ::
			initVarianceParameters(gen))

	def analysisText(prefix: String): String =
		List(
			"deriv <- list(",
			derivations,
			"\t)",
			"# source('mtc.R')",
			"# data <- append.derived(read.mtc('" + prefix + "'), deriv)"
		).mkString("\n")

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

	private def asBasic(p: NetworkModelParameter): BasicParameter = p match {
		case b: BasicParameter => b
		case s: SplitParameter => new BasicParameter(s.base, s.subject)
		case _ => throw new IllegalArgumentException("Cannot convert " + p +
			" to a BasicParameter")
	}

	private def initMetaParameters(g: StartingValueGenerator[M]): List[(String, String)] = {
		val basic = {
			for {basicParam <- model.basicParameters}
			yield g.getRelativeEffect(asBasic(basicParam))
		}
		
		model.parameterVector.map(param => init(param, g, basic))
	}

	private def init(p: NetworkModelParameter, g: StartingValueGenerator[M],
			bl: List[Double])
	: (String, String) = (p.toString, (p match {
		case b: BasicParameter => bl(model.basicParameters.findIndexOf(_ == b))
		case s: SplitParameter => bl(model.basicParameters.findIndexOf(_ == s))
		case i: InconsistencyParameter =>
			InconsistencyStartingValueGenerator(i, model, g, bl)
		case _ => throw new IllegalStateException("Unsupported parameter " + p)
	}).toString)

	private def initBaselineEffects(g: StartingValueGenerator[M]): (String, String) = 
		("mu", JagsSyntaxModel.writeVector(model.studyList.map(s => g.getBaselineEffect(s).asInstanceOf[java.lang.Double]), isJags))

	private def initRelativeEffects(g: StartingValueGenerator[M]): (String, String) = 
		("delta", JagsSyntaxModel.writeMatrix(model.studyList.map(
				s => studyArms(s).map(init(s, _, g))), isJags))
	

	private def init(s: Study[M], t: Treatment, g: StartingValueGenerator[M])
	: java.lang.Double = {
		if (t == null || t == model.studyBaseline(s)) null
		else g.getRandomEffect(s, new BasicParameter(model.studyBaseline(s), t))
	}

	private def initVarianceParameters(g: StartingValueGenerator[M]): List[(String, String)] = {
		("sd.d", g.getRandomEffectsVariance().toString) :: {
			if (inconsistency) List(("sd.w", g.getRandomEffectsVariance().toString))
			else Nil
		}
	}

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

	def modelText: String = {
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

	def scriptText(prefix: String, chains: Int, tuning: Int, simulation: Int)
	: String = {
		val template = {
			if (isJags) readTemplate("jagsScriptTemplate.txt")
			else readTemplate("bugsScriptTemplate.txt")
		}
		val map = new java.util.HashMap[String, Object]()
		map.put("prefix", prefix)
		map.put("nchains", chains.asInstanceOf[AnyRef])
		map.put("chains", asList((1 to chains).map(_.asInstanceOf[AnyRef])))
		map.put("tuning", tuning.asInstanceOf[AnyRef])
		map.put("simulation", simulation.asInstanceOf[AnyRef])
		map.put("inconsistency", inconsistency.asInstanceOf[AnyRef])
		map.put("parameters", asList(model.parameterVector))
		String.valueOf(TemplateRuntime.execute(template, map))
	}

	def maxArmCount: Int = model.studyList.map(x => x.treatments.size).max

	def studyArms(s: Study[M]): List[Treatment] = {
		val baseline = model.studyBaseline(s)
		val arms = baseline :: (s.treatments - baseline).toList.sortWith(_ < _)
		val nonArms: List[Treatment] =
			(0 until (maxArmCount - arms.size)).map(_ => null).toList
		arms ::: nonArms
	}

	def treatmentMatrix: List[List[java.lang.Integer]] = {
		model.studyList.map(s => studyArms(s).map(t =>
			if (t == null) null 
			else model.treatmentMap(t).asInstanceOf[java.lang.Integer]))
	}

	def responderMatrix: List[List[java.lang.Integer]] = {
		model.studyList.map(s => studyArms(s).map(t =>
			if (t == null) null
			else s.measurements(t).asInstanceOf[DichotomousMeasurement].responders.asInstanceOf[java.lang.Integer]))
	}

	def meanMatrix: List[List[java.lang.Double]] = {
		model.studyList.map(s => studyArms(s).map(t =>
			if (t == null) null
			else s.measurements(t).asInstanceOf[ContinuousMeasurement].mean.asInstanceOf[java.lang.Double]))
	}

	def stdErrMatrix: List[List[java.lang.Double]] = {
		model.studyList.map(s => studyArms(s).map(t =>
			if (t == null) null
			else s.measurements(t).asInstanceOf[ContinuousMeasurement].stdErr.asInstanceOf[java.lang.Double]))
	}

	def sampleSizeMatrix: List[List[java.lang.Integer]] = {
		model.studyList.map(s => studyArms(s).map(t =>
			if (t == null) null
			else s.measurements(t).sampleSize.asInstanceOf[java.lang.Integer]))
	}

	def armCounts: List[java.lang.Integer] = {
		model.studyList.map(x => x.treatments.size.asInstanceOf[java.lang.Integer])
	}

	def dataText: String = {
		val list = List(
			("ns", JagsSyntaxModel.writeNumber(model.studyList.size.asInstanceOf[java.lang.Integer], isJags)),
			("na", JagsSyntaxModel.writeVector(armCounts, isJags)),
			("t", JagsSyntaxModel.writeMatrix(treatmentMatrix, isJags))) ++ {
			if (dichotomous) {
				List(
				("r", JagsSyntaxModel.writeMatrix(responderMatrix, isJags)),
				("n", JagsSyntaxModel.writeMatrix(sampleSizeMatrix, isJags)))
			} else {
				List(
				("m", JagsSyntaxModel.writeMatrix(meanMatrix, isJags)),
				("e", JagsSyntaxModel.writeMatrix(stdErrMatrix, isJags)))
			}
		}
		generateDataFile(list)
	}
}

object JagsSyntaxModel {
	/**
	 * Convert a number to a String so that it can be read by S-Plus/R
	 */
	def writeNumber[N <: Number](n: N, jags: Boolean): String = {
		if (n == null) {
			"NA"
		} else if (jags && (n.isInstanceOf[Int] || n.isInstanceOf[Long])) {
			String.valueOf(n) + "L"
		} else {
			String.valueOf(n)
		}
	}

	/**
	 * Convert a matrix m -- where m(i)(j) is the number in the i-th row and j-th column -- to S-Plus/R format.
	 * @param jags true for column-major format (R/S-Plus/JAGS), false for row-major (BUGS).
	 */
	def writeMatrix[N <: Number](m: List[List[N]], jags: Boolean): String = {
		val rows = m.size
		val cols = m(0).size
		val cells: Seq[String] = {
			if (jags) (0 until cols).map(j => (0 until rows).map(i => writeNumber(m(i)(j), jags))).flatten
			else m.flatten.map(writeNumber(_, jags))
		}
		"structure(" + {
			if (jags) "" else ".Data = "
		} + "c(" + cells.mkString(", ") + "), .Dim = c(" + writeNumber[Integer](rows, jags) + ", " + writeNumber[Integer](cols, jags) + "))"
	}

	def writeVector[N <: Number](v: List[N], jags: Boolean): String = {
		"c(" + v.map(writeNumber(_, jags)).mkString(", ") + ")"
	}
}
