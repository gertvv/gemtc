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

package org.drugis.mtc.yadas;

import edu.uci.ics.jung.graph.util.Pair;
import gov.lanl.yadas.ArgumentMaker;
import gov.lanl.yadas.BasicMCMCBond;
import gov.lanl.yadas.ConstantArgument;
import gov.lanl.yadas.Gaussian;
import gov.lanl.yadas.GroupArgument;
import gov.lanl.yadas.IdentityArgument;
import gov.lanl.yadas.MCMCParameter;
import gov.lanl.yadas.MCMCUpdate;
import gov.lanl.yadas.Uniform;
import gov.lanl.yadas.UpdateTuner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.random.JDKRandomGenerator;
import org.drugis.common.threading.AbstractIterativeComputation;
import org.drugis.common.threading.IterativeTask;
import org.drugis.common.threading.NullTask;
import org.drugis.common.threading.SimpleSuspendableTask;
import org.drugis.common.threading.Task;
import org.drugis.common.threading.TaskListener;
import org.drugis.common.threading.activity.ActivityModel;
import org.drugis.common.threading.activity.ActivityTask;
import org.drugis.common.threading.activity.DirectTransition;
import org.drugis.common.threading.activity.ForkTransition;
import org.drugis.common.threading.activity.JoinTransition;
import org.drugis.common.threading.activity.Transition;
import org.drugis.common.threading.event.TaskEvent;
import org.drugis.common.threading.event.TaskEvent.EventType;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MixedTreatmentComparison;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.parameterization.ContinuousDataStartingValueGenerator;
import org.drugis.mtc.parameterization.DichotomousDataStartingValueGenerator;
import org.drugis.mtc.parameterization.InconsistencyParameter;
import org.drugis.mtc.parameterization.InconsistencyVariance;
import org.drugis.mtc.parameterization.NetworkParameter;
import org.drugis.mtc.parameterization.Parameterization;
import org.drugis.mtc.parameterization.RandomEffectsVariance;
import org.drugis.mtc.parameterization.StartingValueGenerator;

abstract class YadasModel implements MixedTreatmentComparison {
	
	private final Network d_network;

	protected Parameterization proto = null;
	protected List<StartingValueGenerator> startingValues = null;
	protected final int nChains = 4;

	private List<List<ParameterWriter>> parameterList = new ArrayList<List<ParameterWriter>>();
	private List<List<MCMCUpdate>> updateList = new ArrayList<List<MCMCUpdate>>();

	protected Parameter randomEffectVar = new RandomEffectsVariance();
	protected Parameter inconsistencyVar = new InconsistencyVariance();

	private int burnInIter = 20000;
	protected int simulationIter = 100000;
	private int reportingInterval = 100;

	private YadasResults results = new YadasResults();
	private ActivityTask d_activityTask;

	private boolean isInconsistency;

	private SimpleSuspendableTask d_finalPhase;

	private class BurnInChain extends AbstractIterativeComputation {
		private final int d_chain;
		
		public BurnInChain(int chain) {
			super(burnInIter);
			d_chain = chain;
		}
		
		public void doStep() {
			update(d_chain);
		}
	}

	private class SimulationChain extends AbstractIterativeComputation {
		private final int d_chain;
		
		public SimulationChain(int chain) {
			super(burnInIter);
			d_chain = chain;
		}
		
		public void doStep() {
			update(d_chain);
			output(d_chain);
		}
	}

	private class BurnInTask extends IterativeTask {
		public BurnInTask(int chain) {
			super(new BurnInChain(chain), "burn-in:" + chain);
			setReportingInterval(reportingInterval);
		}
	}
	
	private class SimulationTask extends IterativeTask {
		public SimulationTask(int chain) {
			super(new SimulationChain(chain), "simulation:" + chain);
			setReportingInterval(reportingInterval);
		}
	}

	public YadasModel(Network network) {
		d_network = network;


		Task buildModelPhase = new SimpleSuspendableTask(new Runnable() {
			public void run() {
				buildModel();
			}
		}, "building model");
		List<Task> burnInPhase = new ArrayList<Task>(nChains);
		List<Task> simulationPhase = new ArrayList<Task>(nChains);
		for (int i = 0; i < nChains; ++i) {
			burnInPhase.add(new BurnInTask(i));
			simulationPhase.add(new SimulationTask(i));
		}
		d_finalPhase = new NullTask();

		List<Transition> transitions = new ArrayList<Transition>();
		transitions.add(new ForkTransition(buildModelPhase, burnInPhase));
		transitions.add(new JoinTransition(simulationPhase, d_finalPhase));
		for (int i = 0; i < nChains; ++i) {
			transitions.add(new DirectTransition(burnInPhase.get(i), simulationPhase.get(i)));
		}
		
		ActivityModel activityModel = new ActivityModel(buildModelPhase, d_finalPhase, transitions);
		d_activityTask = new ActivityTask(activityModel, "MCMC model");
	}

	
	public boolean isReady() {
		return d_finalPhase.isFinished();
	}

	public ActivityTask getActivityTask() {
		return d_activityTask;
	}
	
	@Override
	public BasicParameter getRelativeEffect(Treatment base, Treatment subj) {
		return new BasicParameter(base, subj);
	}

	public int getBurnInIterations() {
		return burnInIter;
	}

	public void setBurnInIterations(int it) {
		validIt(it);
		burnInIter = it;
	}

	public int getSimulationIterations() {
		return simulationIter;
	}

	public void setSimulationIterations(int it) {
		validIt(it);
		simulationIter = it;
	}

	@Override
	public Parameter getRandomEffectsVariance() {
		return randomEffectVar;
	}
	
	public MCMCResults getResults() {
		return results;
	}

	private void validIt(int it) {
		if (it <= 0 || it % 100 != 0) {
			throw new IllegalArgumentException("Specified # iterations should be a positive multiple of 100");
		}
	}

	private double sigmaPrior() {
		return 0.0; // FIXME
	}

	private double inconsSigmaPrior() {
		return 0.0; // FIXME
	}

	protected abstract void buildNetworkModel();

	private void buildModel() {
		buildNetworkModel();
		JDKRandomGenerator rng = new JDKRandomGenerator();
		double scale = 2.5;
		for (int i = 0; i < nChains; ++i) {
			switch (d_network.getType()) {
			case CONTINUOUS:
				startingValues.add(new ContinuousDataStartingValueGenerator(d_network, rng, scale));
				break;
			case RATE:
				startingValues.add(new DichotomousDataStartingValueGenerator(d_network, rng, scale));
				break;
			default:
				throw new IllegalArgumentException("Don't know how to generate starting values for " + d_network.getType() + " data");					
			}
		}

		List<Parameter> parameters = new ArrayList<Parameter>(proto.getParameters());
		parameters.add(randomEffectVar);
		if (isInconsistency) {
			parameters.add(inconsistencyVar);
		}

		results.setDirectParameters(parameters);
		results.setNumberOfChains(nChains);
		results.setNumberOfIterations(simulationIter);

//		FIXME
//		results.setDerivedParameters(
//			indirectParameters.map(p => (p, derivation(p))).toList)
		
		for (int i = 0 ; i < nChains; ++i) {
			createChain(i);
		}

		d_finalPhase.addTaskListener(new TaskListener() {
			@Override
			public void taskEvent(TaskEvent event) {
				if (event.getType() == EventType.TASK_FINISHED) {
					results.simulationFinished();
				}
			}
		});
	}
	

	private void createChain(int chain) {
		StartingValueGenerator startVal = startingValues.get(chain);

		// study baselines
		Map<Study, MCMCParameter> mu = new HashMap<Study, MCMCParameter>();
		for (Study s : d_network.getStudies()) {
			mu.put(s, new MCMCParameter(
					new double[] {startVal.getTreatmentEffect(s, proto.getStudyBaseline(s))},
					new double[] {0.1}, null));
		}
		// random effects
		Map<Study, MCMCParameter> delta = new HashMap<Study, MCMCParameter>();
		for (Study s : d_network.getStudies()) {
			double[] start = new double[reDim(s)];
			double[] step = new double[reDim(s)];
			Arrays.fill(step, 0.1);
			int i = 0;
			for (List<Pair<Treatment>> list : proto.parameterizeStudy(s)) {
				for (Pair<Treatment> pair: list) {
					start[i] = startVal.getRelativeEffect(s, getRelativeEffect(pair.getFirst(), pair.getSecond()));
					++i;
				}
			}
			delta.put(s, new MCMCParameter(start, step, null));
		}
		// basic parameters & inconsistency parameters
		List<NetworkParameter> parameters = proto.getParameters();
		double[] basicStart = new double[parameters.size()];
		double[] basicStep = new double[parameters.size()];
		Arrays.fill(basicStep, 0.1);
		for (int i = 0; i < parameters.size(); ++i) {
			basicStart[i] = getStartingValue(startVal, parameters.get(i));
		}
		MCMCParameter basic = new MCMCParameter(basicStart, basicStep, null);
		// variance
		MCMCParameter sigma = new MCMCParameter(
			new double[] {getStartingVariance(startVal)}, new double[] {0.1}, null);
		// inconsistency variance
		MCMCParameter sigmaw = isInconsistency ? 
				new MCMCParameter(new double[] {getStartingVariance(startVal)}, new double[] {0.1}, null) : null;

		List<MCMCParameter> params = new ArrayList<MCMCParameter>();
		params.addAll(mu.values());
		params.addAll(delta.values());
		params.add(basic);
		params.add(sigma);
		if (isInconsistency) {
			params.add(sigmaw);
		}

		// data bond
		switch (d_network.getType()) {
		case CONTINUOUS:
			continuousDataBond(mu, delta);
			break;
		case RATE:
			dichotomousDataBond(mu, delta);
			break;
		default:
			throw new IllegalArgumentException("Don't know how to handle " + d_network.getType() + " data");					
		}

		// random effects bound to basic/incons parameters
		for (Study study : d_network.getStudies()) {
			relativeEffectBond(study, delta.get(study), basic, sigma);
		}

		// per-study mean prior
		for (Study study : d_network.getStudies()) {
			new BasicMCMCBond(
					new MCMCParameter[] {mu.get(study)},
					new ArgumentMaker[] {
						new IdentityArgument(0),
						new ConstantArgument(0, 1),
						new ConstantArgument(Math.sqrt(getVariancePrior()), 1)
					},
					new Gaussian()
				);
		}

		// basic parameter prior
		int nBasic;
		for (nBasic = 0; nBasic < parameters.size() && !(parameters.get(nBasic) instanceof InconsistencyParameter); ++nBasic) {}
		int[] basicRange = new int[nBasic];
		for (int i = 0; i < nBasic; ++i) {
			basicRange[i] = i;
		}
		new BasicMCMCBond(
				new MCMCParameter[] {basic},
				new ArgumentMaker[] {
					new GroupArgument(0, basicRange), // FIXME: is this even allowed?
					new ConstantArgument(0, nBasic),
					new ConstantArgument(Math.sqrt(getVariancePrior()), nBasic)
				},
				new Gaussian()
			);

		// sigma prior
		new BasicMCMCBond(
				new MCMCParameter[] {sigma},
				new ArgumentMaker[] {
					new IdentityArgument(0),
					new ConstantArgument(0.00001),
					new ConstantArgument(sigmaPrior())
				},
				new Uniform()
			);

		if (isInconsistency) {
			int nIncons = parameters.size() - nBasic;
			int[] inconsRange = new int[nIncons];
			for (int i = 0; i < nIncons; ++i) {
				inconsRange[i] = nBasic + i;
			}
			// inconsistency prior
			new BasicMCMCBond(
					new MCMCParameter[] {basic, sigmaw},
					new ArgumentMaker[] {
						new GroupArgument(0, inconsRange),
						new ConstantArgument(0, nIncons),
						new GroupArgument(1, new int[nIncons])
					},
					new Gaussian()
				);

			// sigma_w prior
			new BasicMCMCBond(
					new MCMCParameter[] {sigmaw},
					new ArgumentMaker[] {
						new IdentityArgument(0),
						new ConstantArgument(0.00001),
						new ConstantArgument(inconsSigmaPrior())
					},
					new Uniform()
				);
		}
		
		List<MCMCUpdate> tuners = new ArrayList<MCMCUpdate>(params.size());
		for (MCMCParameter param : params) {
			tuners.add(new UpdateTuner(param, burnInIter / 50, 50, 1, Math.exp(-1)));
		}

		updateList.set(chain, tuners);

		List<ParameterWriter> writers = new ArrayList<ParameterWriter>(params.size());
		for (int i = 0; i < parameters.size(); ++i) {
			writers.add(results.getParameterWriter(parameters.get(i), chain, basic, i));
		}
		writers.add(results.getParameterWriter(randomEffectVar, chain, sigma, 0));
		if (isInconsistency) {
			writers.add(results.getParameterWriter(inconsistencyVar, chain, sigmaw, 0));
		}

		parameterList.set(chain, writers);
	}


	private double getVariancePrior() {
		// TODO Auto-generated method stub
		return 0;
	}


	private void dichotomousDataBond(Map<Study, MCMCParameter> mu,
			Map<Study, MCMCParameter> delta) {
		// TODO Auto-generated method stub
		
	}


	private void continuousDataBond(Map<Study, MCMCParameter> mu,
			Map<Study, MCMCParameter> delta) {
		// TODO Auto-generated method stub
		
	}


	private double getStartingVariance(StartingValueGenerator startVal) {
		// TODO Auto-generated method stub
		return 0;
	}


	private double getStartingValue(StartingValueGenerator startVal, NetworkParameter networkParameter) {
		// TODO Auto-generated method stub
		return 0;
	}


	private int reDim(Study s) {
		return s.getTreatments().size() - 1;
	}

	private void relativeEffectBond(Study study, MCMCParameter delta,
			MCMCParameter basic, MCMCParameter sigma) {
		ArgumentMaker[] arguments = new ArgumentMaker[2 + reDim(study)];
		arguments[0] = new IdentityArgument(0);
		arguments[1] = new RelativeEffectArgumentMaker(proto, study, 1, -1);

		if (reDim(study) == 1) {
			arguments[2] = new IdentityArgument(3);
			new BasicMCMCBond(
				new MCMCParameter[] {delta, basic, sigma},
				arguments,
				new Gaussian()
			);
		} else {
			List<ArgumentMaker> rows = SigmaRowArgumentMaker.createMatrixArgumentMaker(proto.parameterizeStudy(study), 2);
			for (int i = 0; i < rows.size(); ++i) {
				arguments[2 + i] = rows.get(i);
			}
			new BasicMCMCBond(
				new MCMCParameter[] {delta, basic, sigma},
				arguments,
				new MultivariateGaussian()
			);
		}
	}
/*
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
	}*/

	private void update(int chain) {
		for (MCMCUpdate u : updateList.get(chain)) {
			try {
				u.update();
			} catch(Exception e) {
				throw new RuntimeException("Failed to update " + u, e);
			}
		}
	}

	protected void output(int chain) {
		for (ParameterWriter p : parameterList.get(chain)) {
			p.output();
		}
	}
}