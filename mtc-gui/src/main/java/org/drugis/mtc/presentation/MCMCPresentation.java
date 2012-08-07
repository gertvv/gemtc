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