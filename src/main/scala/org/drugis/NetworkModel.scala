package org.drugis

trait NetworkModelParameter {

}

final class BasicParameter(val base: Treatment, val subject: Treatment)
extends NetworkModelParameter {
	override def toString() = "d." + base.id + "." + subject.id

	override def equals(other: Any): Boolean = other match {
		case p: BasicParameter => (p.base == base && p.subject == subject)
		case _ => false
	}
}

final class InconsistencyParameter(val cycle: List[Treatment])
extends NetworkModelParameter {
	private val cycleStr =
		cycle.reverse.tail.reverse.map(t => t.id).mkString(".")

	override def toString() = "w." + cycleStr

	override def equals(other: Any): Boolean = other match {
		case p: InconsistencyParameter => (p.cycle == cycle)
		case _ => false
	}
}

/**
 * Class representing a Bayes model for a treatment network
 */
class NetworkModel(
	val network: Network, 
	val basis: FundamentalGraphBasis[Treatment],
	val studyBaseline: Map[Study, Treatment],
	val treatmentList: List[Treatment],
	val studyList: List[Study]) {

	require(Set[Study]() ++ studyList == network.studies)
	require(Set[Treatment]() ++ treatmentList == network.treatments)
	require(studyBaseline.keySet == network.studies)
	require(basis.tree.vertexSet == network.treatments)

	val studyMap = NetworkModel.indexMap(studyList)
	val treatmentMap = NetworkModel.indexMap(treatmentList)

	val data = studyList.flatMap(
		study => NetworkModel.treatmentList(study.treatments).map(
				t => (study, study.measurements(t))))

	private val basicParameterEdges = sort(basis.treeEdges)
	private val inconsistencyParameterEdges = sort(inconsistencyEdges)

	private val parameterMap = 
		Map[(Treatment, Treatment), NetworkModelParameter]() ++
		basicParameterEdges.map(e => (e, new BasicParameter(e._1, e._2))) ++
		inconsistencyParameterEdges.map(e => (e, new InconsistencyParameter(
			basis.tree.cycle(e._1, e._2))))

	private val parameterEdges =
		basicParameterEdges ++ inconsistencyParameterEdges

	/**
	 * Gives a list of edges (in the tree) that result directly in parameters
	 * in an ordered manner.
	 */
	val parameterVector: List[NetworkModelParameter] = 
		parameterEdges.map(e => parameterMap(e))

	def parameterization(a: Treatment, b: Treatment)
	: Map[NetworkModelParameter, Int] = param(a, b)

	private def param(a: Treatment, b: Treatment)
	: Map[NetworkModelParameter, Int] = {
		if (basicParameterEdges contains (a, b)) basicParam(a, b)
		else if (basicParameterEdges contains (b, a)) negate(basicParam(b, a))
		else functionalParam(a, b)
	}

	private def functionalParam(a: Treatment, b: Treatment)
	: Map[NetworkModelParameter, Int] = {
		val r = basis.tree.commonAncestor(a, b)
		add(add(negate(pathParam(basis.tree.path(r, a))),
			pathParam(basis.tree.path(r, b))),
			inconsParam(a, b))
	}

	private def inconsParam(a: Treatment, b: Treatment)
	: Map[NetworkModelParameter, Int] = {
		val edges = inconsistencyParameterEdges
		if (edges contains (a, b)) basicParam(a, b)
		else if (edges contains (b, a)) negate(basicParam(b, a))
		else emptyParam()
	}

	private def pathParam(path: List[Treatment])
	: Map[NetworkModelParameter, Int] = {
		if (path.size < 2) emptyParam()
		else add(param(path(0), path(1)), pathParam(path.tail))
	}

	private def basicParam(a: Treatment, b: Treatment) =
		Map[NetworkModelParameter, Int]((parameterMap((a, b)), 1))

	private def negate(p: Map[NetworkModelParameter, Int]) =
		p.transform((a, b) => -b)

	private def add(p: Map[NetworkModelParameter, Int],
			q: Map[NetworkModelParameter, Int])
	: Map[NetworkModelParameter, Int] = {
		emptyParam() ++
		(for {x <- (p.keySet ++ q.keySet)
		} yield (x, getOrZero(p, x) + getOrZero(q, x)))
	}

	private def emptyParam() = Map[NetworkModelParameter, Int]()

	private def getOrZero(p: Map[NetworkModelParameter, Int],
			x: NetworkModelParameter): Int =
	p.get(x) match {
		case None => 0
		case Some(d) => d
	}

	private def inconsistencyEdges: Set[(Treatment, Treatment)] = 
		basis.backEdges.filter(
			e => network.isInconsistency(basis.tree.createCycle(e)))

	private def sort(edges: Set[(Treatment, Treatment)])
	: List[(Treatment, Treatment)] = {
		edges.toList.sort((a, b) => if (a._1 == b._1) a._2 < b._2 else a._1 < b._1)
	}
}

class BaselineSearchState(
	val toCover: Set[(Treatment, Treatment)],
	val studies: List[Study],
	val assignment: Map[Study, Treatment]) { }

class BaselineSearchProblem(
	toCover: Set[(Treatment, Treatment)],
	studies: Set[Study]) extends SearchProblem[BaselineSearchState] {

	val initialState = new BaselineSearchState(toCover, studies.toList,
		Map[Study, Treatment]())

	def isGoal(s: BaselineSearchState): Boolean = {
		s.toCover.isEmpty && s.studies.isEmpty
	}

	def successors(s: BaselineSearchState): List[BaselineSearchState] = {
		if (s.studies.isEmpty) Nil
		else {
			val study = s.studies.head
			(for {t <- study.treatments.toList.sort((a, b) => a < b)
				val toCover = s.toCover -- (
					study.treatmentGraph.edgeSet.filter(e => (e._1 == t || e._2 == t)))
				val assignment = s.assignment + ((study, t))
			} yield new BaselineSearchState(toCover, s.studies.tail, assignment)
			).toList
		}
	}
}

object NetworkModel {
	def apply(network: Network, tree: Tree[Treatment]): NetworkModel = {
		new NetworkModel(network,
			new FundamentalGraphBasis(network.treatmentGraph, tree),
			assignBaselines(network, tree),
			treatmentList(network.treatments),
			studyList(network.studies))
	}

	def apply(network: Network, base: Treatment): NetworkModel = {
		apply(network, network.bestSpanningTree(base))
	}

	def apply(network: Network): NetworkModel = {
		apply(network, treatmentList(network.treatments).first)
	}

	private def assignMultiArm(
		toCover: Set[(Treatment, Treatment)], studies: Set[Study])
	: Map[Study, Treatment] = {
		val problem = new BaselineSearchProblem(toCover, studies)
		val alg = new DFS()
		alg.search(problem) match {
			case None => throw new Exception("No Assignment Found!")
			case Some(x) => x.assignment
		}
	}

	def assignBaselines(network: Network, st: Tree[Treatment])
	: Map[Study, Treatment] = {
		val toCover = network.inconsistencies(st).flatMap(a => a.edgeSet)
		val twoArm = network.studies.filter(study => study.treatments.size == 2)
		val multiArm = network.studies -- twoArm
		val covered = twoArm.flatMap(study => study.treatmentGraph.edgeSet)

		val twoArmMap = Map[Study, Treatment]() ++ twoArm.map(study => (study, study.treatments.toList.sort((a, b) => a < b).head))

		val leftToCover = toCover -- covered
		twoArmMap ++ assignMultiArm(leftToCover, multiArm)
	}

	def studyList(studies: Set[Study]) = {
		studies.toList.sort((a, b) => a.id < b.id)
	}

	def treatmentList(treatments: Set[Treatment]) = {
		treatments.toList.sort((a, b) => a < b)
	}

	def indexMap[T](l: List[T]): Map[T, Int] =
		Map[T, Int]() ++ l.map(a => (a, l.indexOf(a) + 1))
}

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
			parameters,
			empty,
			variances,
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

	private def monitors =
		(paramNames ++ varNames).map(p => "monitor " + p).mkString("\n")

	private def paramNames = model.parameterVector.map(p => p.toString)
	private def varNames = List("var.d", "var.w")

	private val header = "model {"
	private val empty = ""
	private val footer = "}"
	private val baselineEffects = 
	"""	|	# Study baseline effects
		|	for (i in 1:length(b)) {
		|		mu[i] ~ dnorm(0, .001)
		|	}""".stripMargin
	private val individualEffects = 
	"""	|	# For each (study, treatment), model effect
		|	for (i in 1:length(s)) {
		|		logit(p[s[i], t[i]]) <- mu[s[i]] + delta[s[i], b[s[i]], t[i]]
		|		r[i] ~ dbin(p[s[i], t[i]], n[i])
		|	}""".stripMargin
	private val inconsVar =
	"""	|	# Inconsistency variance
		|	sd.w ~ dunif(0.00001, 2)
		|	var.w <- sd.w * sd.w
		|	tau.w <- 1/ var.w""".stripMargin
	private val effectVar =
	"""	|	# Random effect variance
		|	sd.d ~ dunif(0.00001, 2)
		|	var.d <- sd.d * sd.d
		|	tau.d <- 1/ var.d""".stripMargin

	private def deltas: String = 
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

	private def expressParam(p: NetworkModelParameter, v: Int): String = 
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

	private def parameters: String = 
		(
			List("\t# Basic parameters and inconsistencies") ++
			(for {param <- model.parameterVector;
				val tau = param match {
					case basic: BasicParameter => ".001"
					case incon: InconsistencyParameter => "tau.w"
				}} yield "\t" + param.toString + " ~ " + normal("0", tau))
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
				|	tau.2[1, 1] <- tau.d
				|	tau.2[1, 2] <- -tau.d / 2
				|	tau.2[2, 1] <- -tau.d / 2
				|	tau.2[2, 2] <- tau.d""".stripMargin
		else throw new Exception("Studies with > 3 arms not supported yet")

	private def variances: String = 
		(List(
			"\t# Inconsistency variance",
			basicVar("w"),
			empty,
			"\t# Random effect variance",
			basicVar("d")
		) ++ combinedVars).mkString("\n")
}
