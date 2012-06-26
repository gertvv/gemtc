/*
 * This file is part of the GeMTC software for MTC model generation and
 * analysis. GeMTC is distributed from http://drugis.org/gemtc.
 * Copyright (C) 2009-2012 Gert van Valkenhoef.
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

import gov.lanl.yadas.MCMCParameter;
import gov.lanl.yadas.MCMCUpdate;
import gov.lanl.yadas.UpdateTuner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.drugis.common.threading.AbstractExtendableIterativeComputation;
import org.drugis.common.threading.AbstractIterativeComputation;
import org.drugis.common.threading.ExtendableIterativeTask;
import org.drugis.common.threading.IterativeTask;
import org.drugis.common.threading.NullTask;
import org.drugis.common.threading.SimpleSuspendableTask;
import org.drugis.common.threading.Task;
import org.drugis.common.threading.WaitingTask;
import org.drugis.common.threading.activity.ActivityModel;
import org.drugis.common.threading.activity.ActivityTask;
import org.drugis.common.threading.activity.Condition;
import org.drugis.common.threading.activity.DecisionTransition;
import org.drugis.common.threading.activity.DirectTransition;
import org.drugis.common.threading.activity.ForkTransition;
import org.drugis.common.threading.activity.JoinTransition;
import org.drugis.common.threading.activity.Transition;
import org.drugis.mtc.MCMCModel;
import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCSettings;
import org.drugis.mtc.MixedTreatmentComparison;
import org.drugis.mtc.Parameter;

public abstract class AbstractYadasModel implements MCMCModel {
	final class ExtendDecisionTask extends WaitingTask {
		@Override
		public boolean isWaiting() {
			return d_extendSimulation == ExtendSimulation.WAIT;
		}

		@Override
		public void onEndWaiting() {
			d_started = false;
			d_finished = true;
			if (d_extendSimulation == ExtendSimulation.EXTEND) {
				((SimpleRestartableSuspendableTask) d_extendSimulationPhase).reset();
			}
			d_mgr.fireTaskFinished();
		}
		
		public void reset() {
			d_extendSimulation = ExtendSimulation.WAIT;
			d_finished = false;
			d_mgr.fireTaskRestarted();
		}
		
		@Override
		public String toString() {
			return MixedTreatmentComparison.ASSESS_CONVERGENCE_PHASE;
		}
	}

	private class TuningChain extends AbstractIterativeComputation {
		private final int d_chain;
		
		public TuningChain(int chain) {
			super(getTuningIterations());
			d_chain = chain;
		}
		
		public void doStep() {
			update(d_chain);
		}
	}

	private class SimulationChain extends AbstractExtendableIterativeComputation {
		private final int d_chain;
		
		public SimulationChain(int chain) {
			super(getSimulationIterations());
			d_chain = chain;
		}
		
		public void doStep() {
			update(d_chain);
			if(d_iteration % THINNING_INTERVAL == 0) { 
				output(d_chain);
			}
		}
	}

	class TuningTask extends IterativeTask {
		public TuningTask(int chain) {
			super(new TuningChain(chain), TUNING_CHAIN_PREFIX + chain);
			setReportingInterval(d_reportingInterval);
		}
	}
	
	class SimulationTask extends ExtendableIterativeTask {
		public SimulationTask(int chain) {
			super(new SimulationChain(chain), SIMULATION_CHAIN_PREFIX + chain);
			setReportingInterval(d_reportingInterval);
		}
	}
	
	class SimpleRestartableSuspendableTask extends SimpleSuspendableTask {
		public SimpleRestartableSuspendableTask(Runnable runnable, String string) {
			super(runnable, string);
		} 
		
		public void reset() {
			d_started = false;
			d_finished = false;
			d_mgr.fireTaskRestarted();
		}
	}
	
	protected static final int THINNING_INTERVAL = 10;
	protected static final double VARIANCE_SCALING = 2.5;

	protected abstract void createChain(int chain);

	private List<List<ParameterWriter>> d_writeList = new ArrayList<List<ParameterWriter>>();
	private List<List<MCMCUpdate>> d_updateList = new ArrayList<List<MCMCUpdate>>();
	private int d_reportingInterval = 100;
	protected YadasResults d_results = new YadasResults();
	private YadasSettings d_settings = new YadasSettings(20000, 50000, 4);
	private ActivityTask d_activityTask;
	private SimpleSuspendableTask d_finalPhase;
	protected ExtendSimulation d_extendSimulation = ExtendSimulation.WAIT;
	private ExtendDecisionTask d_extendDecisionPhase;
	protected Task d_extendSimulationPhase;
	private SimpleRestartableSuspendableTask d_notifyResults;

	public AbstractYadasModel() {
		buildActivityModel();
	}

	private void buildActivityModel() {
		// Create tasks for each phase of the MCMC simulation
		Task buildModelPhase = new SimpleSuspendableTask(new Runnable() {
			public void run() {
				buildModel();
			}
		}, STARTING_SIMULATION_PHASE);
		final List<Task> tuningPhase = new ArrayList<Task>(getNumberOfChains());
		final List<ExtendableIterativeTask> simulationPhase = new ArrayList<ExtendableIterativeTask>(getNumberOfChains());
		for (int i = 0; i < getNumberOfChains(); ++i) {
			tuningPhase.add(new TuningTask(i));
			simulationPhase.add(new SimulationTask(i));
		}
	
		d_extendDecisionPhase = new ExtendDecisionTask();
				
		d_extendSimulationPhase = new SimpleRestartableSuspendableTask(new Runnable() {
			public void run() {
				// Extend the simulations. This is safe because they won't be started before this task is finished.
				for(ExtendableIterativeTask t : simulationPhase) {
					t.extend(getSimulationIterations());
				}
				d_results.setNumberOfIterations((getSimulationIterations() * 2) / THINNING_INTERVAL);
				d_settings.setSimulationIterations(getSimulationIterations() * 2);

				// Finally, reset the decision phase. Must be done after the simulations are extended, otherwise it becomes a next state.
				d_notifyResults.reset();
				d_extendDecisionPhase.reset();
			}
		}, MixedTreatmentComparison.EXTENDING_SIMULATION_PHASE);
		
		d_finalPhase = new NullTask();
		
		d_notifyResults = new SimpleRestartableSuspendableTask(new Runnable() {	
			@Override
			public void run() {
				d_results.simulationFinished();
			}
		}, MixedTreatmentComparison.CALCULATING_SUMMARIES_PHASE);
		
		// Build transition graph between phases of the MCMC simulation
		List<Transition> transitions = new ArrayList<Transition>();
		transitions.add(new ForkTransition(buildModelPhase, tuningPhase));
		for (int i = 0; i < getNumberOfChains(); ++i) {
			transitions.add(new DirectTransition(tuningPhase.get(i), simulationPhase.get(i)));
		}
		transitions.add(new JoinTransition(simulationPhase, d_notifyResults));
		transitions.add(new DirectTransition(d_notifyResults, d_extendDecisionPhase));
		transitions.add(new DecisionTransition(d_extendDecisionPhase, d_extendSimulationPhase, d_finalPhase, new Condition() {
			public boolean evaluate() {
				return d_extendSimulation == ExtendSimulation.EXTEND;
			}
		}));
		transitions.add(new ForkTransition(d_extendSimulationPhase, simulationPhase));
		// Together they form the full "activity"
		ActivityModel activityModel = new ActivityModel(buildModelPhase, d_finalPhase, transitions);
		d_activityTask = new ActivityTask(activityModel, "MCMC model");
	}

	public boolean isReady() {
		return d_activityTask.isFinished() || d_extendDecisionPhase.isStarted();
	}

	public ActivityTask getActivityTask() {
		return d_activityTask;
	}

	public void setTuningIterations(int it) {
		if (d_activityTask.isStarted()) {
			throw new IllegalAccessError("May not call setTuningIterations() once computations have started.");
		}
		validIt(it);
		d_settings.setTuningIterations(it);
		buildActivityModel();
	}

	public void setSimulationIterations(int it) {
		if (d_activityTask.isStarted()) {
			throw new IllegalAccessError("May not call setTuningIterations() once computations have started.");
		}
		validIt(it);
		d_settings.setSimulationIterations(it);
		buildActivityModel();
	}

	public MCMCResults getResults() {
		return d_results;
	}

	private void validIt(int it) {
		if (it <= 0 || it % 100 != 0) {
			throw new IllegalArgumentException("Specified # iterations should be a positive multiple of 100");
		}
	}

	private void buildModel() {
		prepareModel();
		d_results.setNumberOfChains(getNumberOfChains());
		d_results.setNumberOfIterations(getSimulationIterations() / THINNING_INTERVAL);
		d_results.setDirectParameters(getParameters());
		d_results.setDerivedParameters(getDerivedParameters());	
	
		for (int i = 0 ; i < getNumberOfChains(); ++i) {
			createChain(i);
		}
	}

	abstract protected List<Parameter> getParameters();
	
	protected Map<? extends Parameter, Derivation> getDerivedParameters() {
		return Collections.emptyMap(); 
	}

	/**
	 * After prepareModel(), getParameters and getDirectParameters should return a defined value.
	 */
	abstract protected void prepareModel();

	protected void update(int chain) {
		for (MCMCUpdate u : d_updateList.get(chain)) {
			try {
				u.update();
			} catch(Exception e) {
				throw new RuntimeException("Failed to update " + u, e);
			}
		}
	}

	protected void output(int chain) {
		for (ParameterWriter p : d_writeList.get(chain)) {
			p.output();
		}
	}

	public void setExtendSimulation(ExtendSimulation s) {
		d_extendSimulation = s;
	}

	public MCMCSettings getSettings() {
		return d_settings;
	}

	protected void addWriters(List<ParameterWriter> writers) {
		d_writeList.add(writers);
	}

	protected void addTuners(List<MCMCParameter> params) {
		List<MCMCUpdate> tuners = new ArrayList<MCMCUpdate>(params.size());
		for (MCMCParameter param : params) {
			tuners.add(new UpdateTuner(param, getTuningIterations() / 50, 50, 1, Math.exp(-1)));
		}
		d_updateList.add(tuners);
	}

	private int getTuningIterations() {
		return getSettings().getTuningIterations();
	}

	public int getSimulationIterations() {
		return getSettings().getSimulationIterations();
	}
	
	protected int getNumberOfChains() {
		return getSettings().getNumberOfChains();
	}
}