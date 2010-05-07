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
	private Integer d_max;
	
	public ProgressEvent(EventType type, Integer iter, Integer max) {
		d_type = type;
		d_iter = iter;
		d_max = max;

		if (type.equals(EventType.BURNIN_PROGRESS) || type.equals(EventType.SIMULATION_PROGRESS)) {
			if (d_iter == null || d_max == null) {
				throw new IllegalArgumentException("*_PROGRESS needs to have a specified iteration");
			}
		} else {
			if (d_iter != null || d_max != null) {
				throw new IllegalArgumentException("Only *_PROGRESS can have a specified iteration");
			}
		}
	}

	public ProgressEvent(EventType type) {
		this(type, null, null);
	}

	public EventType getType() {
		return d_type;
	}

	public int getIteration() {
		return d_iter;
	}

	public int getTotalIterations() {
		return d_max;
	}

	@Override
	public String toString() {
		return d_type.toString() + 
			(d_iter == null ? "" : "(" + d_iter.toString() + ")");
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ProgressEvent) {
			ProgressEvent other = (ProgressEvent) o;
			return d_type == other.d_type && equal(d_iter, other.d_iter);
		}
		return false;
	}

	private boolean equal(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		}
		return o1.equals(o2);
	}

	@Override
	public int hashCode() {
		int code = d_type.hashCode() * 31;
		if (d_iter != null) {
			code += d_iter.hashCode();
		}
		return code;
	}
}
