package org.drugis.mtc.presentation;

import org.drugis.common.gui.task.TaskProgressModel;
import org.drugis.common.threading.NullTask;
import org.drugis.common.threading.status.TaskTerminatedModel;
import org.drugis.mtc.MCMCModel;
import org.drugis.mtc.presentation.MCMCModelWrapper;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;

public class MCMCPresentation {
	protected final MCMCModelWrapper d_wrapper;
	protected final String d_name;
	protected final TaskProgressModel d_taskProgressModel;
	protected final ValueModel d_modelConstructionFinished;

	public MCMCPresentation(final MCMCModelWrapper wrapper, final String name) {
		d_wrapper = wrapper;
		d_name = name;
		d_taskProgressModel = !wrapper.isSaved() ? new TaskProgressModel(wrapper.getModel().getActivityTask()) : new TaskProgressModel(new NullTask() {
			public boolean isFinished() {
				return true;
			}
			public boolean isStarted() {
				return true;
			}
		});
		d_modelConstructionFinished = wrapper.isSaved() ? new ValueHolder(true) : 
			new TaskTerminatedModel(wrapper.getModel().getActivityTask().getModel().getStartState());
	}

	public ValueModel isModelConstructed() {
		return d_modelConstructionFinished;
	}

	public boolean hasSavedResults() {
		return d_wrapper.isSaved();
	}

	public TaskProgressModel getProgressModel() {
		return d_taskProgressModel;
	}

	public MCMCModelWrapper getWrapper() {
		return d_wrapper;
	}

	public MCMCModel getModel() {
		return hasSavedResults() ? null : d_wrapper.getModel();
	}

	@Override
	public String toString() { 
		return d_name;
	}
}