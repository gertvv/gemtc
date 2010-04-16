package org.drugis.mtc.yadas

import org.drugis.mtc._
import gov.lanl.yadas._

import org.apache.commons.math.stat.descriptive.moment.StandardDeviation
import org.apache.commons.math.stat.descriptive.moment.Mean
import org.apache.commons.math.linear.ArrayRealVector

class EstimateImpl(val mean: Double, val sd: Double)
extends Estimate {
	def getMean = mean
	def getStandardDeviation = sd

	override def toString = mean.toString + " (" + sd.toString + ")"
}

class Parameter(p: MCMCParameter, i: Int) {
	private var v: List[Double] = List()
	def value = v

	def update() {
		v = p.getValue(i) :: v
	}
}

class YadasModel(proto: NetworkModel[DichotomousMeasurement], isInconsistency: Boolean)
extends ProgressObservable {
	private var ready = false

	private var parameters: Map[NetworkModelParameter, Parameter]
		= null
	protected var results: Map[NetworkModelParameter, Array[Double]]
		= null
	private var paramEstimates: Map[NetworkModelParameter, Estimate]
		= null

	private var parameterList: List[Parameter] = null
	private var updateList: List[MCMCUpdate] = null

	private var randomEffectVar: Parameter = null
	private var inconsistencyVar: Parameter = null

	private var burnInIter = 4000
	protected var simulationIter = 100000
	private var reportingInterval = 100

	def isReady = ready

	def run() {
		// construct model
		notifyModelConstructionStarted()
		buildModel()
		notifyModelConstructionFinished()

		// burn-in iterations
		notifyBurnInStarted()
		burnIn()
		notifyBurnInFinished()

		// simulation iterations
		notifySimulationStarted()
		simulate()

		// calculate results
		calculateResults()
		ready = true

		for (update <- updateList) {
			println("Update " + update + ": " + update.accepted())
		}

		notifySimulationFinished()
	}

	def getRelativeEffect(base: Treatment, subj: Treatment): Estimate =
		if (!isReady) throw new IllegalStateException("Model is not ready")
		else paramEstimate(base, subj) match {
			case Some(x) => x
			case None => throw new IllegalArgumentException(
				"Treatment(s) not found")
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

	private def buildModel() {
		def successArray(network: NetworkModel[DichotomousMeasurement]): Array[Double] =
			network.data.map(m => m._2.asInstanceOf[DichotomousMeasurement].responders.toDouble).toArray

		def sampleSizeArray(network: NetworkModel[DichotomousMeasurement]): Array[Double] =
			network.data.map(m => m._2.sampleSize.toDouble).toArray

		// success-rate r from data
		val r = new ConstantArgument(successArray(proto))
		// sample-size n from data
		val n = new ConstantArgument(sampleSizeArray(proto))
		// study baselines
		val mu = new MCMCParameter(
			Array.make(proto.studyList.size, 0.0),
			Array.make(proto.studyList.size, 0.1),
			null)
		// random effects
		val delta = new MCMCParameter(
			Array.make(proto.relativeEffects.size, 0.0),
			Array.make(proto.relativeEffects.size, 0.1),
			null)
		// basic parameters
		val basic = new MCMCParameter(
			Array.make(proto.basicParameters.size, 0.0),
			Array.make(proto.basicParameters.size, 0.1),
			null)
		// inconsistency parameters
		val incons =
			if (isInconsistency)
				new MCMCParameter(
					Array.make(proto.inconsistencyParameters.size, 0.0),
					Array.make(proto.inconsistencyParameters.size, 0.1),
					null)
			else
				new MCMCParameter(
					Array.make(proto.inconsistencyParameters.size, 0.0),
					Array.make(proto.inconsistencyParameters.size, 0.0),
					null)
		// variance
		val sigma = new MCMCParameter(
			Array(0.25), Array(0.1), null)
		// inconsistency variance
		val sigmaw =
			if (isInconsistency)
				new MCMCParameter(Array(0.25), Array(0.1), null)
			else
				new MCMCParameter(Array(0.0), Array(0.0), null)

		val params =
			if (isInconsistency)
				List[MCMCParameter](mu, delta, basic, incons, sigma, sigmaw)
			else
				List[MCMCParameter](mu, delta, basic, sigma)

		// r_i ~ Binom(p_i, n_i) ; p_i = ilogit(theta_i) ;
		// theta_i = mu_s(i) + delta_s(i)b(i)t(i)
		val databond = new BasicMCMCBond(
				Array[MCMCParameter](mu, delta),
				Array[ArgumentMaker](
					r,
					n,
					new SuccessProbabilityArgumentMaker(proto, 0, 1)
				),
				new Binomial()
			)

		// random effects bound to basic/incons parameters
		val randomeffectbond =  new BasicMCMCBond(
				Array[MCMCParameter](delta, basic, incons, sigma),
				Array[ArgumentMaker](
					new IdentityArgument(0),
					new RelativeEffectArgumentMaker(proto, 1,
						if (isInconsistency) Some(2) else None),
					new GroupArgument(3, Array.make(proto.relativeEffects.size, 0))
				),
				new Gaussian()
			)

		val muprior = new BasicMCMCBond(
				Array[MCMCParameter](mu),
				Array[ArgumentMaker](
					new IdentityArgument(0),
					new ConstantArgument(0, proto.studyList.size),
					new ConstantArgument(Math.sqrt(1000), proto.studyList.size),
				),
				new Gaussian()
			)

		val basicprior = new BasicMCMCBond(
				Array[MCMCParameter](basic),
				Array[ArgumentMaker](
					new IdentityArgument(0),
					new ConstantArgument(0, proto.basicParameters.size),
					new ConstantArgument(Math.sqrt(1000), proto.basicParameters.size),
				),
				new Gaussian()
			)

		val sigmaprior = new BasicMCMCBond(
				Array[MCMCParameter](sigma),
				Array[ArgumentMaker](
					new IdentityArgument(0),
					new ConstantArgument(0.00001),
					new ConstantArgument(2)
				),
				new Uniform()
			)

		if (isInconsistency) {
			val inconsprior = new BasicMCMCBond(
					Array[MCMCParameter](incons, sigmaw),
					Array[ArgumentMaker](
						new IdentityArgument(0),
						new ConstantArgument(0, proto.inconsistencyParameters.size),
						new GroupArgument(1, Array.make(proto.inconsistencyParameters.size, 0))
					),
					new Gaussian()
				)

			val sigmawprior = new BasicMCMCBond(
					Array[MCMCParameter](sigmaw),
					Array[ArgumentMaker](
						new IdentityArgument(0),
						new ConstantArgument(0.00001),
						new ConstantArgument(2)
					),
					new Uniform()
				)
			sigmawprior
		}

		def tuner(param: MCMCParameter): MCMCUpdate =
			new UpdateTuner(param, burnInIter / 50, 50, 1, Math.exp(-1))

		updateList = params.map(p => tuner(p))

		def paramList(p: MCMCParameter, n: Int): List[Parameter] =
			(0 until n).map(i => new Parameter(p, i)).toList

		val basicParam = paramList(basic, proto.basicParameters.size)
		val inconsParam = paramList(incons, proto.inconsistencyParameters.size)
		val sigmaParam = paramList(sigma, 1)
		val sigmawParam = paramList(sigmaw, 1)
		parameterList = basicParam ++ inconsParam ++ sigmaParam ++ sigmawParam

		val basicParamPairs = {
			for (i <- 0 until basicParam.size)
			yield (proto.basicParameters(i), basicParam(i))
		}
		val inconsParamPairs = {
			for (i <- 0 until inconsParam.size)
			yield (proto.inconsistencyParameters(i), inconsParam(i))
		}

		parameters = Map[NetworkModelParameter, Parameter]() ++
			basicParamPairs ++ inconsParamPairs
		
		randomEffectVar = sigmaParam(0)
		inconsistencyVar = sigmawParam(0)
	}

	private def burnIn() {
		for (i <- 0 until burnInIter) {
			if (i % reportingInterval == 0 && i / reportingInterval > 0)
				notifyBurnInProgress(i);

			update()
		}
	}

	private def simulate() {
		for (i <- 0 until simulationIter) {
			if (i % reportingInterval == 0 && i / reportingInterval > 0)
				notifySimulationProgress(i);

			update()
			output()
		}
	}

	private def update() {
		for (u <- updateList) {
			u.update()
		}
	}

	private def output() {
		for (p <- parameterList) {
			p.update()
		}
	}

	private def calculateResults() {
		preCalculateResults()

		val ts = proto.treatmentList
		paramEstimates = Map[NetworkModelParameter, Estimate]() ++
			(for {(p, v) <- results} yield (p, summary(v)))
	}

	private def preCalculateResults() {
		val ts = proto.treatmentList
		results = Map[NetworkModelParameter, Array[Double]]() ++
		(for {i <- 0 until (ts.size - 1); j <- (i + 1) until ts.size;
			val p = new BasicParameter(ts(i), ts(j))
		} yield (p, preCalculateResult(p))) ++
		(for {p <- proto.inconsistencyParameters
		} yield (p, preCalculateResult(p)))
	}

	private def preCalculateResult(p: NetworkModelParameter): Array[Double] = {
		if (proto.basicParameters.contains(p) ||
				proto.inconsistencyParameters.contains(p))
			parameters(p).value.toArray
		else p match {
			case bp: BasicParameter =>
				calcValue(proto.parameterization(bp.base, bp.subject))
			case _ => throw new IllegalStateException()
		}
	}

	private def nullFields() {
		parameters = null
		parameterList = null
		updateList = null
		randomEffectVar = null
		inconsistencyVar = null
	}

	private def calcValue(pz: Map[NetworkModelParameter, Int])
	: Array[Double] = {
		var value = new ArrayRealVector(simulationIter)
		for ((p, f) <- pz) {
			if (f != 0) {
				val pv = new ArrayRealVector(parameters(p).value.toArray)
				pv.mapMultiplyToSelf(f)
				value = value.add(pv)
			}
		}
		return value.getData()
	}

	private def summary(result: Array[Double]): Estimate = {
		new EstimateImpl((new Mean()).evaluate(result),
			(new StandardDeviation()).evaluate(result))
	}
}
