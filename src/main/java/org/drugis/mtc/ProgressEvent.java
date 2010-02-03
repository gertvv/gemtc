package org.drugis.mtc;

public class ProgressEvent {
	public enum EventType {
		MODEL_CONSTRUCTION_STARTED,
		MODEL_CONSTRUCTION_FINISHED,
		BURNIN_STARTED,
		BURNIN_PROGRESS,
		BURNIN_FINISHED,
		SIMULATION_STARTED,
		SIMULATION_PROGRESS,
		SIMULATION_FINISHED
	}

	private EventType d_type;
	private Integer d_iter;
	
	public ProgressEvent(EventType type, Integer iter) {
		d_type = type;
		d_iter = iter;

		if (type.equals(EventType.BURNIN_PROGRESS) || type.equals(EventType.SIMULATION_PROGRESS)) {
			if (d_iter == null) {
				throw new IllegalArgumentException("*_PROGRESS needs to have a specified iteration");
			}
		} else {
			if (d_iter != null) {
				throw new IllegalArgumentException("Only *_PROGRESS can have a specified iteration");
			}
		}
	}

	public ProgressEvent(EventType type) {
		this(type, null);
	}

	public EventType getType() {
		return d_type;
	}

	public int getIteration() {
		return d_iter;
	}

	@Override
	public String toString() {
		return d_type.toString() + 
			(d_iter == null ? "" : "(" + d_iter.toString() + ")");
	}
}
