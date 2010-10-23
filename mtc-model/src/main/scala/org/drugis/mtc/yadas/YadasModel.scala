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
import org.drugis.common.threading.IterativeComputation
import org.drugis.common.threading.IterativeTask
import org.drugis.common.threading.SimpleSuspendableTask
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

	protected var parameters: Map[NetworkModelParameter, Parameter] = null

	private var parameterList: List[ParameterWriter] = null
	private var updateList: List[MCMCUpdate] = null

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
	
	private val burnInPhase = new IterativeTask(new IterativeComputation() {
		private var iter = 0
		def initialize() {}
		def finish() {}
		def step() { update(); iter += 1; }
		def getIteration(): Int = iter
		def getTotalIterations(): Int = burnInIter
	}, "burn-in")
	burnInPhase.setReportingInterval(reportingInterval)
	
	private val simulationPhase = new IterativeTask(new IterativeComputation() {
		private var iter = 0 
		def initialize() {}
		def finish() {}
		def step() { update(); output(); iter += 1; }
		def getIteration(): Int = iter
		def getTotalIterations(): Int = simulationIter
	}, "simulation")
	simulationPhase.setReportingInterval(reportingInterval)
	
	val activityModel = new ActivityModel(
			buildModelPhase, // start
			simulationPhase, // end
			ArrayBuffer[Transition]( // transitions
				new DirectTransition(buildModelPhase, burnInPhase),
				new DirectTransition(burnInPhase, simulationPhase)
			)
		)

	val activityTask = new ActivityTask(activityModel, "MCMC model")
	
	def isReady = simulationPhase.isFinished()

	def getActivityTask: ActivityTask = activityTask
	
	def getRelativeEffect(base: Treatment, subj: Treatment): Parameter =
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
			new BasicMCMCBond(
				Array[MCMCParameter](delta, basic, incons, sigma),
				(baseArguments ::: SigmaMatrixArgumentMaker(study, 3)).toArray,
				new MultivariateGaussian()
			)
		}
	}

	protected def buildNetworkModel();

	private def buildModel() {
		buildNetworkModel()
		
		// study baselines
		val mu = Map[Study[M], MCMCParameter]() ++
			proto.studyList.map(s => (s, new MCMCParameter(
				Array(0.0),
				Array(0.1),
				null)))
		// random effects
		val delta = Map[Study[M], MCMCParameter]() ++
			proto.studyList.map(s => (s, new MCMCParameter(
				Array.make(reDim(s), 0.0),
				Array.make(reDim(s), 0.1),
				null)))
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

		updateList = params.map(p => tuner(p))

		def writerList(p: MCMCParameter, l: List[Parameter])
		: List[ParameterWriter] =
			(0 until l.size).map(i => results.getParameterWriter(l(i), 0, p, i)
				).toList

		val parameters =
			proto.basicParameters ++
			proto.inconsistencyParameters ++
			List(randomEffectVar, inconsistencyVar)

		results.setDirectParameters(parameters)
		results.setNumberOfChains(1)
		results.setNumberOfIterations(simulationIter)

		val writers = 
			writerList(basic, proto.basicParameters) ++
			writerList(incons, proto.inconsistencyParameters) ++
			writerList(sigma, List(randomEffectVar)) ++
			writerList(sigmaw, List(inconsistencyVar))

		parameterList = writers

		results.setDerivedParameters(
			indirectParameters.map(p => (p, derivation(p))).toList)
	}

/*
	private def parameterMap(basicMap: Map[NetworkModelParameter, MyParameter])
	:Map[NetworkModelParameter, MyParameter] = {
		val ts = proto.treatmentList
		basicMap ++ (
		for {i <- 0 until (ts.size - 1); j <- (i + 1) until ts.size;
			val p = new BasicParameter(ts(i), ts(j));
			if (!basicMap.keySet.contains(p))
		} yield (p, createIndirect(p, basicMap)))
	}
*/
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

	private def update() {
		for (u <- updateList) {
			u.update()
		}
	}

	protected def output() {
		for (p <- parameterList) {
			p.output()
		}
	}
}
