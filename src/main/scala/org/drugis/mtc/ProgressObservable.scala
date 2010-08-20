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

package org.drugis.mtc

import ProgressEvent.EventType._
import org.drugis.common.threading._

trait ProgressObservable extends AbstractSuspendable
with MixedTreatmentComparison {
	private var listeners = List[ProgressListener]()
	def addProgressListener(l: ProgressListener) {
		listeners += l
	}

	protected def notifyListeners(e: ProgressEvent) {
		for (l <- listeners) {
			l.update(this, e)
		}
	}

	protected def notifyModelConstructionStarted() {
		notifyListeners(new ProgressEvent(MODEL_CONSTRUCTION_STARTED))
	}

	protected def notifyModelConstructionFinished() {
		notifyListeners(new ProgressEvent(MODEL_CONSTRUCTION_FINISHED))
	}

	protected def notifyBurnInStarted() {
		notifyListeners(new ProgressEvent(BURNIN_STARTED))
	}

	protected def notifyBurnInFinished() {
		notifyListeners(new ProgressEvent(BURNIN_FINISHED))
	}

	protected def notifyBurnInProgress(iteration: Int) {
		notifyListeners(new ProgressEvent(BURNIN_PROGRESS, iteration,
			getBurnInIterations()))
	}

	protected def notifySimulationStarted() {
		notifyListeners(new ProgressEvent(SIMULATION_STARTED))
	}

	protected def notifySimulationFinished() {
		notifyListeners(new ProgressEvent(SIMULATION_FINISHED))
	}

	protected def notifySimulationProgress(iteration: Int) {
		notifyListeners(new ProgressEvent(SIMULATION_PROGRESS, iteration,
			getSimulationIterations()))
	}
}
