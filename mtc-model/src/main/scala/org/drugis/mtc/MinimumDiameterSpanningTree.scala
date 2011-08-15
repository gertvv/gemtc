/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
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

/**
 * Find the minimum diameter spanning tree
 */
object MinimumDiameterSpanningTree {
	def apply[T <% Ordered[T]](graph: UndirectedGraph[T]): Tree[T] = {
		val sp = ShortestPath.calculate(graph)
		val x = AbsoluteOneCenter(graph, sp)
		val paths = (graph.vertexSet - x.u - x.v).map(w => Graph.pathEdges(shortestPath(sp, x, w)))
		val xEdge = {
			if (x.t == 0.5) {
				if (x.u < x.v) (x.u, x.v)
				else (x.v, x.u)
			} else if (x.t < 0.5) (x.u, x.v)
			else (x.v, x.u)
		}
		val edges = {
			if (paths.size > 1) paths.reduceLeft((a, b) => a ++ b) + xEdge
			else if (paths.size == 1) paths.toList.apply(0) + xEdge
			else Set(xEdge)
		}
		new Tree(edges, xEdge._1)
	}

	def shortestPath[T <% Ordered[T]](sp: ShortestPath[T], x: PointCenter[T], w: T): List[T] = {
		val Some(d1) = sp.distance(x.u, w)
		val Some(d2) = sp.distance(x.v, w)
		val Some(p) = {
			if (d1 + x.t <= d2 + 1 - x.t) {
				sp.path(x.u, w)
			} else {
				sp.path(x.v, w)
			}
		}
		p
	}
}

case class PointCenter[T <% Ordered[T]](u: T, v: T, t: Double)

/**
 * Find the absolute 1-center of an undirected graph G.
 *
 * Let e = (u,v) be an edge of the graph, and let x(e) be a point along the
 * edge, with t = t(x(e)) \in [0, 1] the distance of x(e) from u.
 * Then, for any point x on G, d(v, x) is the lenght of the shortest path in G
 * between the vertex v and point x.
 * Define F(x) = \max_{v \in V} d(v, x), and x* = \arg\min_{x on G} F(x).
 * x* is the absolute 1-center of G and F(x*) is the absolute 1-radius of G.
 *
 * Algorithm from Kariv and Hakimi (1979), SIAM J Appl Math 37 (3): 513-538.
 */
object AbsoluteOneCenter {
	def apply[T <% Ordered[T]](graph: UndirectedGraph[T], sp: ShortestPath[T]): PointCenter[T] = {
		val dm = sp.distanceMatrix
		val l = distanceOrderedVertices(dm)

		def convert(e: (T, T)): (Int, Int) = {
			(sp.index.indexOf(e._1), sp.index.indexOf(e._2))
		}

		def findLocal(e: (T, T)): ((T, T), Double, Double) = {
			val (t, r) = localCenter(convert(e), dm, l)
			(e, t, r)
		}

		val edges = graph.edgeVector.map(findLocal _)
		val center = edges.min(Ordering[Double].on[((T,T),Double,Double)](_._3))
		PointCenter(center._1._1, center._1._2, center._2)
	}

	def distanceOrderedVertices(dm: List[List[Double]]): List[List[Int]] =
		(0 until dm.size).map(i => distanceSort(i, dm)).toList

	/**
	 * Sort the vertices of G is non-increasing order of distance from v_i.
	 */
	def distanceSort[T <% Ordered[T]](i: Int, dm: List[List[Double]]): List[Int] = {
		def lt(a: Int, b: Int): Boolean = {
			val da = dm(i)(a)
			val db = dm(i)(b)
			if (da == db) a < b // distances equal, sort by index
			else da > db // distances not equal, sort by distance
		}
		(0 until dm.size).sortWith(lt).toList
	}

	/**
	 * Find the point t* \in (0, 1) where D_e(u, t) = D_e(v, t), and D_e(u, t) and D_e(v, t) have
	 * opposite signs (if it exists).
	 */
	def intersect(dm: List[List[Double]], e: (Int, Int), u: Int, v: Int): Option[Double] = {
		val (l, r) = e
		val (lu, ru, lv, rv) = (dm(l)(u), dm(r)(u), dm(l)(v), dm(r)(v))

		if (lu == lv || ru == rv) None // they coincide or intersect only at the edge
		else if (lu > lv && ru > rv) None // u dominates v
		else if (lu < lv && ru < rv) None // v dominates u
		else {
			val t1 = 0.5 * (rv - lu + 1)
			val t2 = 0.5 * (ru - lv + 1)
			if (t1 + lu <= 1 - t1 + ru) Some(t1)
			else Some(t2)
		}
	}

	/**
	 * Find the local one-center x*(e) of e = (v_r, v_s).
	 * @return (t(x*(e)), r(x*(e)))
	 */
	def localCenter(e: (Int, Int), dm: List[List[Double]], l: List[List[Int]]): (Double, Double) = {
		val (vr, vs) = e

		/**
		 * distance between two vertices v_i and v_j
		 */
		def d(i: Int, j: Int) = dm(i)(j)

		/**
		 * distance between vertex v_i and point x = x((v_r, v_s)) with t(x) = t
		 */
		def de(i: Int, t: Double) = Math.min(t + d(vr, i), 1 - t + d(vs, i))

		/**
		 * Perform step 3 of the algorithm (treatment of vertices v s.t. D_e(v, 0) = D_e(v_1, 0)).
		 * @param t The suspected one-center
		 * @param r The suspected one-radius
		 * @param vM (v_m)
		 */
		def step3(t: Double, r: Double, vM: Int, l: List[Int]): (Double, Double) = {
			val v = l.head // guaranteed to succeed
			if (de(v, 0) != de(vM, 0)) step4(t, r, vM, l)
			else if (de(v, 1) > de(vM, 1)) step3(t, r, v, l.tail) // v_m <- v*
			else step3(t, r, vM, l.tail)
		} 

		/**
		 * Perform step4 of the algorithm
		 */
		def step4(t: Double, r: Double, vM: Int, l: List[Int]): (Double, Double) = {
			val vBar = vM
			(l: @unchecked) match {
				case v :: Nil => step8(t, r, vBar, v)
				case v :: tail => step5(t, r, v, vBar, tail)
			}
		}

		/**
		 * Perform step5 of the algorithm (find all vertices v s.t. D_e(v, 0) = D_e(v_i, 0) and find
		 * the corresponding v_m).
		 * @param vBar (\bar{v}^*)
		 */
		def step5(t: Double, r: Double, vM: Int, vBar: Int, l: List[Int]): (Double, Double) = {
			var v = l.head
			if (de(v, 0) != de(vM, 0)) step6(t, r, vM, vBar, l)
			else if (de(v, 1) > de(vM, 1)) step5(t, r, v, vBar, l.tail) // v_m <- v*
			else step5(t, r, vM, vBar, l.tail)
		}

		/**
		 * Find where D_e(v_i, \cdot) and d_e(v_j, \cdot) intersect.
		 */
		def intersectE(i: Int, j: Int): Option[Double] = {
			intersect(dm, e, i, j)
		}

		def step6(t: Double, r: Double, vM: Int, vBar: Int, l: List[Int]): (Double, Double) = {
			intersectE(vM, vBar) match {
				case None => step7(t, r, vM, vBar, l)
				case Some(tM) => {
					if (de(vM, tM) < r) step7(tM, de(vM, tM), vM, vBar, l)
					else step7(t, r, vM, vBar, l)
				}
			}
		}

		def step7(t: Double, r: Double, vM: Int, vBar: Int, l: List[Int]): (Double, Double) = {
			val vBarNew = {
				if (de(vM, 1) > de(vBar, 1)) vM
				else vBar
			}
			(l: @unchecked) match {
				case v :: Nil => step8(t, r, vBarNew, v)
				case v :: tail => step5(t, r, v, vBarNew, tail)
			}
		}

		/**
		 * Perform step8 of the algorithm (treatment of v_n)
		 */
		def step8(t: Double, r: Double, vBar: Int, vN: Int): (Double, Double) = {
			intersectE(vN, vBar) match {
				case None => (t, r)
				case Some(tM) => {
					val rN = de(vN, tM)
					if (rN < r) (tM, rN)
					else (t, r)
				}
			}
		}

		val (t, r) = {
			if (de(l(vr).head, 0) <= de(l(vs).head, 1)) {
				(0, de(l(vr).head, 0))
			} else {
				(1, de(l(vs).head, 1))
			}
		}
		if (l(vr).head == l(vs).head) (t, r)
		else step3(t, r, l(vr).head, l(vr).tail)
	}
}
