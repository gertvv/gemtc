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
import org.drugis.mtc.MCMCSettingsCache;
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

	private class BurnInChain extends AbstractIterativeComputation {
		private final int d_chain;
		
		public BurnInChain(int chain) {
			super(d_tuningIter);
			d_chain = chain;
		}
		
		public void doStep() {
			update(d_chain);
		}
	}

	private class SimulationChain extends AbstractExtendableIterativeComputation {
		private final int d_chain;
		
		public SimulationChain(int chain) {
			super(d_simulationIter);
			d_chain = chain;
		}
		
		public void doStep() {
			update(d_chain);
			output(d_chain);
		}
	}

	class BurnInTask extends IterativeTask {
		public BurnInTask(int chain) {
			super(new BurnInChain(chain), "Tuning: " + chain);
			setReportingInterval(d_reportingInterval);
		}
	}
	
	class SimulationTask extends ExtendableIterativeTask {
		public SimulationTask(int chain) {
			super(new SimulationChain(chain), "Simulation: " + chain);
			setReportingInterval(d_reportingInterval);
		}
	}
	
	class SimpleRestartableSuspendableTask extends SimpleSuspendableTask {
		public SimpleRestartableSuspendableTask(Runnable runnable, String string) {
			super(runnable, string);
		} 
		
		public void reset() {
			d_finished = false;
			d_mgr.fireTaskRestarted();
		}
	}
	
	protected static final int THINNING_INTERVAL = 1;
	protected static final double VARIANCE_SCALING = 2.5;

	protected abstract void createChain(int chain);

	protected final int d_nChains = 4;
	private List<List<ParameterWriter>> d_writeList = new ArrayList<List<ParameterWriter>>();
	private List<List<MCMCUpdate>> d_updateList = new ArrayList<List<MCMCUpdate>>();
	private int d_tuningIter = 20000;
	private int d_simulationIter = 60000;
	private int d_reportingInterval = 100;
	protected YadasResults d_results = new YadasResults();
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
		final List<Task> burnInPhase = new ArrayList<Task>(d_nChains);
		final List<ExtendableIterativeTask> simulationPhase = new ArrayList<ExtendableIterativeTask>(d_nChains);
		for (int i = 0; i < d_nChains; ++i) {
			burnInPhase.add(new BurnInTask(i));
			simulationPhase.add(new SimulationTask(i));
		}
	
		d_extendDecisionPhase = new ExtendDecisionTask();
				
		d_extendSimulationPhase = new SimpleRestartableSuspendableTask(new Runnable() {
			public void run() {
				// Extend the simulations. This is safe because they won't be started before this task is finished.
				for(ExtendableIterativeTask t : simulationPhase) {
					t.extend(d_simulationIter);
				}
				d_results.setNumberOfIterations(d_results.getNumberOfIterations() + d_simulationIter);
				// Finally, reset the decision phase. Must be done last otherwise it becomes a next state.
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
		transitions.add(new ForkTransition(buildModelPhase, burnInPhase));
		for (int i = 0; i < d_nChains; ++i) {
			transitions.add(new DirectTransition(burnInPhase.get(i), simulationPhase.get(i)));
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

	public int getBurnInIterations() {
		return d_tuningIter;
	}

	public void setBurnInIterations(int it) {
		validIt(it);
		d_tuningIter = it;
		buildActivityModel();
	}

	public int getSimulationIterations() {
		return d_simulationIter;
	}

	public void setSimulationIterations(int it) {
		validIt(it);
		d_simulationIter = it;
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
		d_results.setNumberOfChains(d_nChains);
		d_results.setNumberOfIterations(getSimulationIterations());
		d_results.setDirectParameters(getParameters());
		d_results.setDerivedParameters(getDerivedParameters());	
	
		for (int i = 0 ; i < d_nChains; ++i) {
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

	public MCMCSettingsCache getSettings() {
		return new MCMCSettingsCache(d_simulationIter / (2 * THINNING_INTERVAL), d_simulationIter, 
				THINNING_INTERVAL, d_tuningIter, VARIANCE_SCALING, d_nChains);
	}

	protected void addWriters(List<ParameterWriter> writers) {
		d_writeList.add(writers);
	}

	protected void addTuners(List<MCMCParameter> params) {
		List<MCMCUpdate> tuners = new ArrayList<MCMCUpdate>(params.size());
		for (MCMCParameter param : params) {
			tuners.add(new UpdateTuner(param, d_tuningIter / 50, 50, 1, Math.exp(-1)));
		}
		d_updateList.add(tuners);
	}
}