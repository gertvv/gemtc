package org.drugis.mtc.jags

import org.drugis.mtc._
import fr.iarc.jags.model.Model
import fr.iarc.jags.model.Node
import fr.iarc.jags.model.Monitor

class EstimateImpl(val mean: Double, val sd: Double)
extends Estimate {
	def getMean = mean
	def getStandardDeviation = sd
}

class JagsJniInconsistencyModel(proto: NetworkModel)
extends InconsistencyModel {
	private var ready = false
	private var model: Model = null

	private var paramNodes: Map[NetworkModelParameter, Node] = null
	private var paramMonitors: Map[NetworkModelParameter, Monitor] = null
	private var paramEstimates: Map[NetworkModelParameter, EstimateImpl] = null

	def getRelativeEffect(base: Treatment, subj: Treatment): Estimate =
		if (!isReady) throw new IllegalStateException("Model is not ready")
		else paramEstimate(base, subj) match {
			case Some(x) => x
			case None => throw new IllegalArgumentException(
				"Treatment(s) not found")
		}

	def addProgressListener(l: ProgressListener) {
		throw new RuntimeException("Not Implemented")
	}

	def isReady = ready

	def run() {
		buildModel()
		model.initialize(false)
		model.update(3000)
		model.stopAdapting()
		attachMonitors()
		model.update(2000)
		calculateResults()
		ready = true
	}

	def getInconsistencyFactors: java.util.List[InconsistencyParameter] = {
		val list = new java.util.ArrayList[InconsistencyParameter]()
		for (param <- proto.inconsistencyParameters) {
			list.add(param)
		}
		list
	}

	def getInconsistency(param: InconsistencyParameter): Estimate =
		if (!isReady) throw new IllegalStateException("Model is not ready")
		else paramEstimates.get(param) match {
			case Some(x: EstimateImpl) => x
			case None => throw new IllegalArgumentException(
				"Inconsistency not found")
		}

	private def buildModel() {
		model = new Model(1)
		val baselines = buildBaselineEffects

		paramNodes = buildParameterNodes

		val tau = addTau(addVar()) // RE variance
		for (study <- proto.studyList) {
			// effects are the mu + delta for each treatment
			val effects = studyTreatmentEffects(study, baselines(study), tau)
			addStudyMeasurements(study, effects)
		}

		println("done building model")
	}

	private def buildBaselineEffects: Map[Study, Node] = 
		Map[Study, Node]() ++
		(for {s <- proto.studyList;
			val base = addNormal(0.0, 0.001)
		} yield (s, base))

	private def buildParameterNodes: Map[NetworkModelParameter, Node] = {
		val effectTau = addConstant(0.001)
		val inconsTau = addTau(addVar())
		Map[NetworkModelParameter, Node]() ++
		(for {param <- proto.parameterVector;
			val tau = param match {
				case basic: BasicParameter => effectTau
				case incon: InconsistencyParameter => inconsTau
			}} yield (param, addNormal(addConstant(0), tau)))
	}

	private def addStudyMeasurements(s: Study, effects: Map[Treatment, Node]) {
		for (t <- s.treatments) {
			val p = addILogit(effects(t))
			val n = addConstant(s.measurements(t).sampleSize.toDouble)
			addBinomial(p, n, s.measurements(t).responders.toDouble)
		}
	}

	/**
	 * Construct the effect estimate mu_study + delta_study_treatment
	 */
	private def studyTreatmentEffects(s: Study, b: Node, tau: Node)
	: Map[Treatment, Node] = {
		val delta = studyDeltas(s, b, tau)
		Map[Treatment, Node]() ++
		(for {t <- s.treatments} yield (t,
			treatmentEffect(b, delta(t))))
	}

	/**
	 * Construct the delta_study_treatment nodes (give null where delta = 0)
	 */
	private def studyDeltas(s: Study, b: Node, tau: Node)
	: Map[Treatment, Node] = {
		if (s.treatments.size == 2) twoArmDeltas(s, b, tau)
		else multiArmDeltas(s, b, tau)
	}

	private def twoArmDeltas(s: Study, b: Node, tau: Node)
	: Map[Treatment, Node] = {
		Map[Treatment, Node](
			(base(s), null),
			(subj(s), addNormal(express(s, subj(s)), tau))
		)
	}

	private def base(study: Study) = proto.studyBaseline(study)

	private def subj(study: Study) = {
		require(study.treatments.size == 2)
		(study.treatments - base(study)).toList.first
	}

	private def multiArmDeltas(s: Study, b: Node, tau: Node)
	: Map[Treatment, Node] = throw new RuntimeException("Not Implemented")

	/**
	 * Give treatment effect mu_study + delta_study_treatment
	 */
	private def treatmentEffect(b: Node, d: Node) = {
		if (d == null) b
		else addAdd(b, d)
	}

	private def addNormal(mean: Double, tau: Double): Node =
		model.addStochasticNode("dnorm",
			Array[Node](addConstant(mean), addConstant(tau)),
			null, null, null)

	private def addNormal(mean: Node, tau: Node): Node =
		model.addStochasticNode("dnorm",
			Array[Node](mean, tau),
			null, null, null)

	private def addBinomial(p: Node, n: Node, data: Double): Node =
		model.addStochasticNode("dbin",
			Array[Node](p, n),
			null, null, Array(data))

	private def addConstant(value: Double): Node = {
		model.addConstantNode(Array(1), Array(value))
	}

	private def addTau(variance: Node): Node = {
		val one = addConstant(1)
		model.addDeterministicNode("/", Array[Node](one, variance))
	}

	private def addVar(): Node = {
		val sd = addUniform(0.00001, 2)
		model.addDeterministicNode("*", Array[Node](sd, sd))
	}

	private def addUniform(p1: Double, p2: Double): Node = 
		model.addStochasticNode("dunif", 
			Array[Node](addConstant(p1), addConstant(p2)),
			null, null, null)

	private def addAdd(a: Node, b: Node): Node =
		model.addDeterministicNode("+", Array[Node](a, b))

	private def addNeg(a: Node): Node =
		model.addDeterministicNode("NEG", Array[Node](a))

	private def addILogit(a: Node): Node =
		model.addDeterministicNode("ilogit", Array[Node](a))

	private def attachMonitors() {
		paramMonitors = Map[NetworkModelParameter, Monitor]() ++
		(for {(p, n) <- paramNodes
		} yield (p, model.addTraceMonitor(n)))
	}

	private def calculateResults() {

	}

	private def paramEstimate(base: Treatment, subj: Treatment)
	: Option[Estimate] =
		paramEstimates.get(new BasicParameter(base, subj)) match {
			case Some(x: EstimateImpl) => Some[Estimate](x)
			case None => negParamEstimate(subj, base)
		}

	private def negParamEstimate(base: Treatment, subj: Treatment)
	: Option[Estimate] =
		paramEstimates.get(new BasicParameter(base, subj)) match {
			case Some(x: EstimateImpl) =>
				Some[Estimate](new EstimateImpl(-x.mean, x.sd))
			case None => None
		}

	private def expressParam(p: NetworkModelParameter, v: Int): Node = 
		v match {
			case  1 => paramNodes(p)
			case -1 => addNeg(paramNodes(p))
			case  _ => throw new Exception("Unexpected value!")
		}

	def expressParams(params: Map[NetworkModelParameter, Int]): Node =
		(for {(p, v) <- params} yield expressParam(p, v)).reduceLeft(addAdd)

	def express(study: Study, effect: Treatment) = {
		val base = proto.studyBaseline(study)
		require(effect != base)
		expressParams(proto.parameterization(base, effect))
	}
}
