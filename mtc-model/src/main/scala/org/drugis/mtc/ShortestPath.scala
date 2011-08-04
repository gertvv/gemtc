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
 * The <a href="http://en.wikipedia.org/wiki/Floyd-Warshall_algorithm">Floyd–Warshall algorithm</a> to find the all-pairs shortest paths in a Graph.
 */
class ShortestPath[T <% Ordered[T]](_graph: Graph[T]) {
	private val graph: Graph[T] = _graph.directedGraph
	val index = graph.vertexSet.toList.sorted(new Ordering[T] {
		def compare(x: T, y: T) = x compareTo y
	})
	val n = graph.vertexSet.size
	private val dist = Array.ofDim[Int](n, n)
	private val next = Array.ofDim[Int](n, n)

	private def calculate() {
		// Initialization
		for (i <- 0 until n) {
			for (j <- 0 until n) {
				next(i)(j) = -1
				if (i == j) {
					dist(i)(j) = 0
				} else if (graph.containsEdge((index(i), index(j)))) {
					dist(i)(j) = 1
				} else {
					dist(i)(j) = -1
				}
			}
		}

		// Updating
		for (k <- 0 until n) {
			for (i <- 0 until n) {
				for (j <- 0 until n) {
					if (less(plus(dist(i)(k), dist(k)(j)), dist(i)(j))) {
						dist(i)(j) = plus(dist(i)(k), dist(k)(j))
						next(i)(j) = k
					}
				}
			}
		}
	}

	def less(x: Int, y: Int): Boolean =
		if (x == -1) false
		else if (y == -1) true
		else x < y

	def min(x: Int, y: Int): Int =
		if (x == -1) y
		else if (y == -1) x
		else Math.min(x, y)

	def plus(x: Int, y: Int): Int = 
		if (x == -1 || y == -1) -1
		else x + y

	def distance(v: T, u: T): Option[Int] = {
		val x = dist(index.indexOf(v))(index.indexOf(u))
		if (x == -1) None
		else Some(x)
	}

	private def distanceByIndex(i: Int, j: Int): Double = {
		val x = dist(i)(j)
		if (x == -1) Double.PositiveInfinity
		else x.toDouble
	}

	def distanceMatrix: List[List[Double]] = {
		(0 until n).map(i =>
			(0 until n).map(j => distanceByIndex(i, j)).toList
		).toList
	}

	def path(u: T, v: T): Option[List[T]] = {
		getPath(index.indexOf(u), index.indexOf(v)) match {
			case None => None
			case Some(l) => Some(List(u) ::: l.map(i => index(i)) ::: List(v))
		}
	}

	private def getPath(i: Int, j: Int): Option[List[Int]] = {
		if (dist(i)(j) == -1) None
		else if (next(i)(j) == -1) Some(Nil)
		else {
			val k = next(i)(j)
			val path1 = getPath(i, next(i)(j))
			val path2 = getPath(next(i)(j), j)
			path1 match {
				case None => None
				case Some(l1) => path2 match {
					case None => None
					case Some(l2) => Some(l1 ::: List(k) ::: l2)
				}
			}
		}
	}
}

object ShortestPath {
	def calculate[T <% Ordered[T]](graph: Graph[T]) = {
		val sp = new ShortestPath(graph)
		sp.calculate()
		sp
	}
}
