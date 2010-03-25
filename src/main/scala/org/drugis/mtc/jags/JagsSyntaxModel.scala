package org.drugis.mtc.jags

import org.drugis.mtc._

abstract class JagsSyntaxModel(model: NetworkModel) {
	def dataText: String =
		List(
			vectorStr("s", studyIndexVector),
			vectorStr("t", treatmentIndexVector),
			vectorStr("r", responderVector),
			vectorStr("n", sampleSizeVector),
			vectorStr("b", baselineVector)
		).mkString("\n")

	private def studyIndexVector: List[Int] =
		model.data.map(a => model.studyMap(a._1))

	private def treatmentIndexVector: List[Int] =
		model.data.map(a => model.treatmentMap(a._2.treatment))

	private def responderVector: List[Int] = 
		model.data.map(a => a._2.responders)

	private def sampleSizeVector: List[Int] =
		model.data.map(a => a._2.sampleSize)

	private def baselineVector: List[Int] = 
		model.studyList.map(s => model.treatmentMap(model.studyBaseline(s)))

	private def vectorStr(name: String, vector: List[Int]): String = {
		name + " <- c(" + vector.mkString(", ") + ")"
	}

	def modelText: String
	def scriptText(prefix: String): String
	def analysisText(prefix: String): String
}

class JagsSyntaxConsistencyModel(model: NetworkModel)
extends JagsSyntaxInconsistencyModel(model) {
	override def modelText: String = {
		List(
			header,
			baselineEffects,
			empty,
			deltas,
			empty,
			individualEffects,
			empty,
			basicParameters,
			empty,
			randomEffectsVariance,
			footer).mkString("\n")
	}

	override def expressParams(params: Map[NetworkModelParameter, Int])
	: String =
		(for {(p, v) <- params; if (p.isInstanceOf[BasicParameter])} yield expressParam(p, v)).mkString(" + ")

	override def monitors =
		(model.basicParameters.map(p => p.toString) + "var.d").map(p => "monitor " + p).mkString("\n")

	override def analysisText(prefix: String): String =
		List(
			"source('" + prefix + ".R')",
			"attach(trace)",
			"data <- list()",
			standardizeParameters("data"),
			"data$var.d <- var.d",
			"detach(trace)"
		).mkString("\n")
}

class JagsSyntaxInconsistencyModel(model: NetworkModel)
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
			basicParameters,
			empty,
			inconsistencyFactors,
			empty,
			inconsistencyVariance,
			empty,
			randomEffectsVariance,
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
			standardizeInconsistencies("data"),
			standardizeVariances("data"),
			"detach(trace)"
		).mkString("\n")

	protected def monitors =
		(paramNames ++ varNames).map(p => "monitor " + p).mkString("\n")

	private def paramNames = model.parameterVector.map(p => p.toString)
	private def varNames = List("var.d", "var.w")

	protected val header = "model {"
	protected val empty = ""
	protected val footer = "}"
	protected val baselineEffects = 
	"""	|	# Study baseline effects
		|	for (i in 1:length(b)) {
		|		mu[i] ~ dnorm(0, .001)
		|	}""".stripMargin
	protected val individualEffects = 
	"""	|	# For each (study, treatment), model effect
		|	for (i in 1:length(s)) {
		|		logit(p[s[i], t[i]]) <- mu[s[i]] + delta[s[i], b[s[i]], t[i]]
		|		r[i] ~ dbin(p[s[i], t[i]], n[i])
		|	}""".stripMargin
	protected val inconsVar =
	"""	|	# Inconsistency variance
		|	sd.w ~ dunif(0.00001, 2)
		|	var.w <- sd.w * sd.w
		|	tau.w <- 1/ var.w""".stripMargin
	protected val effectVar =
	"""	|	# Random effect variance
		|	sd.d ~ dunif(0.00001, 2)
		|	var.d <- sd.d * sd.d
		|	tau.d <- 1/ var.d""".stripMargin

	protected def deltas: String = 
		(for {
			study <- model.studyList
		} yield studyDeltas(study)).mkString("\n")

	private def studyDeltas(study: Study): String = 
		if (study.treatments.size == 2) twoArmDeltas(study)
		else multiArmDeltas(study)

	private def twoArmDeltas(study: Study) =
		List(
			"\t# Random effects in study " + study.id,
			"\t" + zeroDelta(study, base(study)),
			"\t" + delta(study, subj(study)) + " ~ " +
				normal(express(study, subj(study)), "tau.d")
		).mkString("\n")

	private def subj(study: Study) = {
		require(study.treatments.size == 2)
		(study.treatments - base(study)).toList.first
	}

	private def multiArmDeltas(study: Study) = {
		val treatments = (study.treatments - base(study)).toList
		List(
			"\t# Random effects in study " + study.id,
			paramArray(study, treatments),
			randomEffectArray(study, treatments.size),
			"\t" + zeroDelta(study, base(study)),
			deltasArray(study, treatments)).mkString("\n")
	}

	private def paramArray(study: Study, treatments: List[Treatment]) = 
		(for {
			k <- 1 to treatments.size
		} yield "\td[" + idx(study) + ", " + k + "] <- " +
			express(study, treatments(k - 1))).mkString("\n")

	private def randomEffectArray(study: Study, n: Int) = 
		"\tre[" + idx(study) + ", 1:" + n + "] ~ " +
			"dmnorm(d[" + idx(study) + ", 1:" + n + "], tau." + n + ")"

	private def deltasArray(study: Study, treatments: List[Treatment]) = 
		(for {k <- 1 to treatments.size} yield
			"\t" + delta(study, treatments(k - 1)) + " <- " +
			"re[" + idx(study) + ", " + k + "]").mkString("\n")

	private def normal(mean: String, tau: String) =
		"dnorm(" + mean + ", " + tau + ")"

	private def zeroDelta(study: Study, subj: Treatment) =
		delta(study, subj) + " <- 0"

	private def delta(study: Study, subj: Treatment) = "delta[" +
		idx(study) + ", " + idx(base(study)) + ", " + idx(subj) + "]"

	private def idx(study: Study) = model.studyMap(study)

	private def idx(treatment: Treatment) = model.treatmentMap(treatment)

	private def base(study: Study) = model.studyBaseline(study)

	protected def expressParam(p: NetworkModelParameter, v: Int): String = 
		v match {
			case  1 => p.toString
			case -1 => "-" + p.toString
			case  _ => throw new Exception("Unexpected value!")
		}

	def expressParams(params: Map[NetworkModelParameter, Int]): String =
		(for {(p, v) <- params} yield expressParam(p, v)).mkString(" + ")

	def express(study: Study, effect: Treatment) = {
		val base = model.studyBaseline(study)
		require(effect != base)
		expressParams(model.parameterization(base, effect))
	}

	protected def basicParameters: String = 
		(
			List("\t# Basic parameters") ++
			(for {param <- model.basicParameters
				} yield "\t" + param.toString + " ~ " + normal("0", ".001"))
		).mkString("\n")

	protected def inconsistencyFactors: String =
		(
			List("\t# Inconsistency factors") ++
			(for {param <- model.inconsistencyParameters
				} yield "\t" + param.toString + " ~ " + normal("0", "tau.w"))
		).mkString("\n")


	private def basicVar(name: String) =
		"""	|	sd.x ~ dunif(0.00001, 2)
			|	var.x <- sd.x * sd.x
			|	tau.x <- 1 / var.x""".stripMargin.replaceAll("x", name)

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
		(for {edge <- model.network.edgeVector
			val p = new BasicParameter(edge._1, edge._2)
			val e = expressParams(model.parameterization(edge._1, edge._2))
		 } yield frame + "$" + p + " <- " + e).mkString("\n")

	private def standardizeInconsistencies(frame: String) = 
		(for {param <- model.parameterVector;
			if (param.isInstanceOf[InconsistencyParameter])
		} yield frame + "$" + param + " <- " + param).mkString("\n")
			

	protected def standardizeVariances(frame: String) = 
		List(
			frame + "$var.d <- var.d",
			frame + "$var.w <- var.w"
		).mkString("\n")
}
