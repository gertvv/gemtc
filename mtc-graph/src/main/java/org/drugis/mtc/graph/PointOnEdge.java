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

package org.drugis.mtc.graph;

/**
 * Represents a point x along an edge e = (v0, v1), with t \in [0, l(e)] the distance of x from v0.  
 */
public class PointOnEdge<V, E> {
	private final E d_e;
	private final V d_v0;
	private final V d_v1;
	private final double d_t;

	public PointOnEdge(E e, V v0, V v1, double t) {
		d_e = e;
		d_v0 = v0;
		d_v1 = v1;
		d_t = t;
	}
	
	/**
	 * Get the edge along which this point is placed.
	 */
	public E getEdge() {
		return d_e;
	}
	
	/**
	 * Get the start vertex v0.
	 */
	public V getVertex0() {
		return d_v0;
	}
	
	/**
	 * Get the end vertex v1.
	 */
	public V getVertex1() {
		return d_v1;
	}
	
	/**
	 * Get the distance t \in [0, l(e)] from the start vertex v0.
	 * Thus, if t = 0.0, x = v0 and if t = l(e), x = v1. Otherwise x lies in between the two.
	 * @return t.
	 */
	public double getDistance() {
		return d_t;
	}
	
	@Override
	public String toString() {
		return "PointOnEdge(edge=" + getEdge() + ", v0=" + getVertex0() + ", v1=" + getVertex1() + ", t=" + getDistance() + ")";
	}
}
