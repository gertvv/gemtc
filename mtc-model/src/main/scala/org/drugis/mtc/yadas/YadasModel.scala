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
import org.drugis.common.threading.activity.Transition
import org.drugis.common.threading.activity.DirectTransition
import org.drugis.common.threading.activity.ForkTransition
import org.drugis.common.threading.activity.JoinTransition
import org.drugis.common.threading.IterativeComputation
import org.drugis.common.threading.IterativeTask
import org.drugis.common.threading.AbstractIterativeComputation
import org.drugis.common.threading.TaskListener
import org.drugis.common.threading.event.TaskEvent
import org.drugis.common.threading.event.TaskEvent.EventType
import org.drugis.common.threading.SimpleSuspendableTask
import org.drugis.common.threading.NullTask
import org.drugis.common.threading.activity.ActivityModel
import org.drugis.common.threading.activity.ActivityTask
import org.drugis.mtc._
import gov.lanl.yadas._

import collection.JavaConversions._

import org.apache.commons.math.stat.descriptive.moment.StandardDeviation
import org.apache.commons.math.stat.descriptive.moment.Mean
import org.apache.commons.math.linear.ArrayRealVector

abstract class YadasModel[M <: Measurement, P <: Parametrization[M]](
	network: Network[M],
	isInconsistency: Boolean) {
	val dichotomous: Boolean = {
		val cls = network.measurementType
		if (cls == classOf[DichotomousMeasurement])
			true
		else if (cls == classOf[ContinuousMeasurement])
			false
		else
			throw new IllegalStateException("Unknown measurement type " + cls)
	}

	protected var proto: NetworkModel[M, P] = null
	protected var startingValues: List[StartingValueGenerator[M]] = null

	val nChains = 2

	protected var parameters: Map[NetworkModelParameter, Parameter] = null

	private val parameterList: Array[List[ParameterWriter]] = 
		Array.fill(nChains)(null.asInstanceOf[List[ParameterWriter]])
	private val updateList: Array[List[MCMCUpdate]] =
		Array.fill(nChains)(null.asInstanceOf[List[MCMCUpdate]])

	protected val randomEffectVar: Parameter = new RandomEffectsVariance()
	protected val inconsistencyVar: Parameter = new InconsistencyVariance()

	private var burnInIter = 20000
	protected var simulationIter = 100000
	private var reportingInterval = 100

	private val results = new YadasResults()

	private val buildModelPhase = new SimpleSuspendableTask(new Runnable() {
		def run() {
			buildModel();
		}
	}, "building model")

	private class BurnInChain(val chain: Int)
	extends AbstractIterativeComputation(burnInIter) {
		def doStep() { update(chain); }
	}

	private class SimulationChain(val chain: Int)
	extends AbstractIterativeComputation(simulationIter) {
		def doStep() { update(chain); output(chain); }
	}

	private class BurnInTask(val chain: Int)
	extends IterativeTask(new BurnInChain(chain), "burn-in:" + chain) {
		setReportingInterval(reportingInterval)
	}

	private class SimulationTask(val chain: Int)
	extends IterativeTask(new SimulationChain(chain), "simulation:" + chain) {
		setReportingInterval(reportingInterval)
	}

	private val burnInPhase =
		(0 until nChains).map(c => new BurnInTask(c)).toList
	private val simulationPhase =
		(0 until nChains).map(c => new SimulationTask(c)).toList

	private val finalPhase = new NullTask();

	private val transitions: ArrayBuffer[Transition] =
			ArrayBuffer[Transition]( // transitions
				new ForkTransition(buildModelPhase, asBuffer(burnInPhase)),
				new JoinTransition(asBuffer(simulationPhase), finalPhase)
			) ++ (0 until nChains).map(c =>
					new DirectTransition(burnInPhase(c), simulationPhase(c)))

	val activityModel = new ActivityModel(
			buildModelPhase, // start
			finalPhase, // end
			transitions
		)

	val activityTask = new ActivityTask(activityModel, "MCMC model")
	
	def isReady = finalPhase.isFinished()

	def getActivityTask: ActivityTask = activityTask
	
	def getRelativeEffect(base: Treatment, subj: Treatment): BasicParameter =
		new BasicParameter(base, subj)

	def getBurnInIterations: Int = burnInIter

	def setBurnInIterations(it: Int) {
		validIt(it)
		burnInIter = it
	}

	def getSimulationIterations: Int = simulationIter

	def setSimulationIterations(it: Int) {
		validIt(it)
		simulationIter = it
	}

	def getRandomEffectsVariance: Parameter = randomEffectVar

	def getResults: MCMCResults = results

	private def validIt(it: Int) {
		if (it <= 0 || it % 100 != 0) throw new IllegalArgumentException("Specified # iterations should be a positive multiple of 100");
	}

	private def sigmaPrior = {
		proto.variancePrior
	}

	private def inconsSigmaPrior = {
		sigmaPrior
	}

	private def reIndexArray(s: Study[M]): Array[Int] = {
		val first = proto.relativeEffectIndex(s)
		val last = first + reDim(s) 
		(first to last).toArray
	}

	private def reDim(s: Study[M]): Int = {
		s.treatments.size - 1
	}

	private def values(map: Map[Study[M], MCMCParameter]) = {
		map.map(x => x._2)
	}

	private def invert[T](e: (T, T)) = (e._2, e._1)

	private def splitNode(study: Study[M])
	: Option[(Treatment, Treatment)] = proto.parametrization match {
		case splt: NodeSplitParametrization[M] => {
			val re = proto.studyRelativeEffects(study)
			val splitNode = splt.splitNode
			if (re.contains(splitNode)) Some(splitNode)
			else if (re.contains(invert(splitNode))) Some(invert(splitNode))
			else None
		}
		case _ => None
	}

	private def relativeEffectBond(study: Study[M], delta: MCMCParameter,
			basic: MCMCParameter, incons: MCMCParameter, sigma: MCMCParameter,
			isInconsistency: Boolean) = {
		val baseArguments = List(
				new IdentityArgument(0),
				new RelativeEffectArgumentMaker(proto, 1,
					if (isInconsistency) Some(2) else None, study)
			)

		if (reDim(study) == 1) {
			new BasicMCMCBond(
				Array[MCMCParameter](delta, basic, incons, sigma),
				(baseArguments ::: List(new IdentityArgument(3))).toArray,
				new Gaussian()
			)
		} else {
			val sigmaArgument = splitNode(study) match {
				case Some(node) => SigmaMatrixArgumentMaker(study, proto.studyRelativeEffects(study).findIndexOf(x => x == node), 3)
				case None => SigmaMatrixArgumentMaker(study, 3)
			}
			new BasicMCMCBond(
				Array[MCMCParameter](delta, basic, incons, sigma),
				(baseArguments ::: sigmaArgument).toArray,
				new MultivariateGaussian()
			)
		}
	}

	protected def buildNetworkModel();

	private def buildModel() {
		buildNetworkModel()
		startingValues =
			List(new PriorStartingValueGenerator(proto),
				DataStartingValueGenerator(proto))

		val parameters =
			proto.basicParameters ++
			proto.inconsistencyParameters ++ {
				if (isInconsistency) List(randomEffectVar, inconsistencyVar)
				else List(randomEffectVar)
			}

		results.setDirectParameters(parameters)
		results.setNumberOfChains(nChains)
		results.setNumberOfIterations(simulationIter)

		results.setDerivedParameters(
			indirectParameters.map(p => (p, derivation(p))).toList)
		
		for (chain <- 0 until nChains) {
			createChain(chain)
		}

		finalPhase.addTaskListener(new TaskListener() {
			def taskEvent(event: TaskEvent) {
				if (event.getType() == EventType.TASK_FINISHED) {
					results.simulationFinished()
				}
			}
		})
	}

	private def basicParameter(p: NetworkModelParameter) = p match {
		case b: BasicParameter => b
		case s: SplitParameter => new BasicParameter(s.base, s.subject)
		case _ => throw new IllegalStateException()
	}

	// FIXME: implement
	private def inconsistencyStartingValue(p: InconsistencyParameter,
		startVal: StartingValueGenerator[M], basicStart: List[Double])
	: Double = {
		InconsistencyStartingValueGenerator(p, proto, startVal, basicStart)
	}

	private def createChain(chain: Int) {
		val startVal = startingValues(chain)

		// study baselines
		val mu = Map[Study[M], MCMCParameter]() ++
			proto.studyList.map(s => (s, new MCMCParameter(
				Array(startVal.getBaselineEffect(s)),
				Array(0.1),
				null)))
		// random effects
		val delta = Map[Study[M], MCMCParameter]() ++
			proto.studyList.map(s => (s, new MCMCParameter(
				proto.studyRelativeEffects(s).map(p => startVal.getRandomEffect(s, getRelativeEffect(p._1, p._2))).toArray,
				Array.make(reDim(s), 0.1),
				null)))
		// basic parameters
		val basicStart = proto.basicParameters.map(p => startVal.getRelativeEffect(basicParameter(p)))
		val basic = new MCMCParameter(
			basicStart.toArray,
			Array.make(proto.basicParameters.size, 0.1),
			null)
		// inconsistency parameters
		val incons =
			if (isInconsistency)
				new MCMCParameter(
					proto.inconsistencyParameters.map(p => inconsistencyStartingValue(p, startVal, basicStart)).toArray,
					Array.make(proto.inconsistencyParameters.size, 0.1),
					null)
			else
				new MCMCParameter(
					Array.make(proto.inconsistencyParameters.size, 0.0),
					Array.make(proto.inconsistencyParameters.size, 0.0),
					null)
		// variance
		val sigma = new MCMCParameter(
			Array(startVal.getRandomEffectsVariance()), Array(0.1), null)
		// inconsistency variance
		val sigmaw =
			if (isInconsistency)
				new MCMCParameter(Array(startVal.getRandomEffectsVariance()), Array(0.1), null)
			else
				new MCMCParameter(Array(0.0), Array(0.0), null)

		val params = List[MCMCParameter]() ++ values(mu) ++ values(delta) ++ {
			if (isInconsistency)
				 List(basic, incons, sigma, sigmaw)
			else
				List(basic, sigma)
		}

		// data bond
		if (dichotomous)
			dichotomousDataBond(
				proto.asInstanceOf[NetworkModel[DichotomousMeasurement, _]],
				mu.asInstanceOf[Map[Study[DichotomousMeasurement], MCMCParameter]],
				delta.asInstanceOf[Map[Study[DichotomousMeasurement], MCMCParameter]])
		else
			continuousDataBond(
				proto.asInstanceOf[NetworkModel[ContinuousMeasurement, _]],
				mu.asInstanceOf[Map[Study[ContinuousMeasurement], MCMCParameter]],
				delta.asInstanceOf[Map[Study[ContinuousMeasurement], MCMCParameter]])

		// random effects bound to basic/incons parameters
		for (study <- proto.studyList) {
			relativeEffectBond(study, delta(study),
					basic, incons, sigma,
					isInconsistency)
		}

		// per-study mean prior
		for (study <- proto.studyList) {
			new BasicMCMCBond(
					Array[MCMCParameter](mu(study)),
					Array[ArgumentMaker](
						new IdentityArgument(0),
						new ConstantArgument(0, 1),
						new ConstantArgument(Math.sqrt(1000), 1)
					),
					new Gaussian()
				)
		}

		// basic parameter prior
		new BasicMCMCBond(
				Array[MCMCParameter](basic),
				Array[ArgumentMaker](
					new IdentityArgument(0),
					new ConstantArgument(0, proto.basicParameters.size),
					new ConstantArgument(Math.sqrt(1000), proto.basicParameters.size)
				),
				new Gaussian()
			)

		// sigma prior
		new BasicMCMCBond(
				Array[MCMCParameter](sigma),
				Array[ArgumentMaker](
					new IdentityArgument(0),
					new ConstantArgument(0.00001),
					new ConstantArgument(sigmaPrior)
				),
				new Uniform()
			)

		if (isInconsistency) {
			// inconsistency prior
			new BasicMCMCBond(
					Array[MCMCParameter](incons, sigmaw),
					Array[ArgumentMaker](
						new IdentityArgument(0),
						new ConstantArgument(0, proto.inconsistencyParameters.size),
						new GroupArgument(1, Array.make(proto.inconsistencyParameters.size, 0))
					),
					new Gaussian()
				)

			// sigma_w prior
			new BasicMCMCBond(
					Array[MCMCParameter](sigmaw),
					Array[ArgumentMaker](
						new IdentityArgument(0),
						new ConstantArgument(0.00001),
						new ConstantArgument(inconsSigmaPrior)
					),
					new Uniform()
				)
		}

		def tuner(param: MCMCParameter): MCMCUpdate =
			new UpdateTuner(param, burnInIter / 50, 50, 1, Math.exp(-1))

		updateList(chain) = params.map(p => tuner(p))

		def writerList(p: MCMCParameter, l: List[Parameter])
		: List[ParameterWriter] =
			(0 until l.size).map(i => results.getParameterWriter(l(i), chain,
				p, i)
				).toList

		val writers = 
			writerList(basic, proto.basicParameters) ++
			writerList(incons, proto.inconsistencyParameters) ++
			writerList(sigma, List(randomEffectVar)) ++ {
				if (isInconsistency) writerList(sigmaw, List(inconsistencyVar))
				else Nil
			}

		parameterList(chain) = writers
	}

	private def indirectParameters: Seq[BasicParameter] = {
		val ts = proto.treatmentList
		ts.map(t => (ts - t).map(u => new BasicParameter(t, u))).reduceLeft((a, b) => a ++ b) -- proto.basicParameters.asInstanceOf[List[BasicParameter]]
	}

	private def derivation(p: BasicParameter)
	: Derivation = {
		val param = Map[Parameter, Int]() ++
			proto.parametrization(p.base, p.subject).filter((x) => x._2 != 0)
		new Derivation(param)
	}

	private def successArray(model: NetworkModel[DichotomousMeasurement, _],
		study: Study[DichotomousMeasurement])
	: Array[Double] =
		NetworkModel.treatmentList(study.treatments).map(t =>
			study.measurements(t).responders.toDouble).toArray

	private def sampleSizeArray(model: NetworkModel[DichotomousMeasurement, _],
		study: Study[DichotomousMeasurement])
	: Array[Double] =
		NetworkModel.treatmentList(study.treatments).map(t =>
			study.measurements(t).sampleSize.toDouble).toArray

	private def dichotomousDataBond(model: NetworkModel[DichotomousMeasurement, _],
			mu: Map[Study[DichotomousMeasurement], MCMCParameter],
			delta: Map[Study[DichotomousMeasurement], MCMCParameter]) {
		// r_i ~ Binom(p_i, n_i) ; p_i = ilogit(theta_i) ;
		// theta_i = mu_s(i) + delta_s(i)b(i)t(i)
		for (study <- model.studyList) {
			// success-rate r from data
			val r = new ConstantArgument(successArray(model, study))
			// sample-size n from data
			val n = new ConstantArgument(sampleSizeArray(model, study))
			new BasicMCMCBond(
					Array[MCMCParameter](mu(study), delta(study)),
					Array[ArgumentMaker](
						r,
						n,
						new SuccessProbabilityArgumentMaker(model, 0, 1, study)
					),
					new Binomial()
				)
		}
	}

	private def obsMeanArray(model: NetworkModel[ContinuousMeasurement, _],
		study: Study[ContinuousMeasurement])
	: Array[Double] =
		NetworkModel.treatmentList(study.treatments).map(t =>
			study.measurements(t).mean).toArray

	private def obsErrorArray(model: NetworkModel[ContinuousMeasurement, _],
		study: Study[ContinuousMeasurement])
	: Array[Double] =
		NetworkModel.treatmentList(study.treatments).map(t =>
			study.measurements(t).stdErr).toArray

	private def continuousDataBond(model: NetworkModel[ContinuousMeasurement, _],
			mu: Map[Study[ContinuousMeasurement], MCMCParameter],
			delta: Map[Study[ContinuousMeasurement], MCMCParameter]) {
		for (study <- model.studyList) {
			// success-rate r from data
			val m = new ConstantArgument(obsMeanArray(model, study))
			// sample-size n from data
			val s = new ConstantArgument(obsErrorArray(model, study))

			// m_i ~ N(theta_i, s_i)
			// theta_i = mu_s(i) + delta_s(i)b(i)t(i)
			new BasicMCMCBond(
					Array[MCMCParameter](mu(study), delta(study)),
					Array[ArgumentMaker](
						m,
						new ThetaArgumentMaker(model, 0, 1, study),
						s
					),
					new Gaussian()
				)
		}
	}

	private def update(chain: Int) {
		for (u <- updateList(chain)) {
			try {
				u.update()
			} catch {
				case e => throw new RuntimeException("Failed to update " + u, e)
			}
		}
	}

	protected def output(chain: Int) {
		for (p <- parameterList(chain)) {
			p.output()
		}
	}
}
