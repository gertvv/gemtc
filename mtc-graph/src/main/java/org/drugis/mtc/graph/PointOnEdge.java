package org.drugis.mtc.graph;

/**
 * Represents a point x along an edge e = (v0, v1), with t \in [0, 1] the distance of x from v0.  
 */
public class PointOnEdge<V, E> {
	private final E d_e;
	private final V d_v0;
	private final V d_v1;
	private final double d_t;

	public PointOnEdge(E e, V v0, V v1, double t) {
		if (t < 0.0 || t > 1.0) {
			throw new IllegalArgumentException("The distance t must be in [0, 1]; was " + t);
		}
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
	 * Get the distance t \in [0, 1] from the start vertex v0.
	 * Thus, if t = 0.0, x = v0 and if t = 1.0, x = v1. Otherwise x lies in between the two.
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
