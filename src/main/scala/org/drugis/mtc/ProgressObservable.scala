package org.drugis.mtc

import ProgressEvent.EventType._

trait ProgressObservable extends MixedTreatmentComparison {
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
		notifyListeners(new ProgressEvent(BURNIN_PROGRESS, iteration))
	}

	protected def notifySimulationStarted() {
		notifyListeners(new ProgressEvent(SIMULATION_STARTED))
	}

	protected def notifySimulationFinished() {
		notifyListeners(new ProgressEvent(SIMULATION_FINISHED))
	}

	protected def notifySimulationProgress(iteration: Int) {
		notifyListeners(new ProgressEvent(SIMULATION_PROGRESS, iteration))
	}
}
