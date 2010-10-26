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

class SpanningTreeSearchListenerImpl extends SpanningTreeSearchListener {
	var iter = 0
	def receive(tree: Tree[Treatment], icd: Integer, max: Boolean,
			assignment: Option[Boolean], best: Boolean) {
		println(tree.edgeSet + "; " + icd + "; " + max + "; " +
				assignment + "; " + best)
		iter = iter + 1
	}
}

class SearchTestImpl(xmlFile: String) {
	val xml = scala.xml.XML.loadFile(xmlFile)
	val network = Network.fromXML(xml)
	val top = network.treatments.toList.sort((a, b) => a < b).first 

	def run() {
		val t0 = System.currentTimeMillis()
		val listener = new SpanningTreeSearchListenerImpl()
		val result = network.searchSpanningTree(top, listener)
		val best = result
		val model = InconsistencyNetworkModel(network, best)
		val t1 = System.currentTimeMillis()
		println("Enumerated " + listener.iter)
		println("Solution: ")
		println(best.dotString)
		println("ICDF " + model.parametrization.inconsistencyDegree)
		val v = network.treatmentGraph.vertexSet.size
		println("|V| " + v)
		val e = network.treatmentGraph.edgeSet.size
		println("|E| " + e)
		val s = e - v + 1
		println("S " + s)
		val k = (v * (v - 1)) / 2
		println("K " + k)
		val dt = t1 - t0
		println("dt " + dt)

		val basis = new FundamentalGraphBasis(network.treatmentGraph, best)
		println(basis.dotString)
		println(model.parametrization.inconsistencyClasses)
	}
}

class EnumerateTestImpl(xmlFile: String) {
	val xml = scala.xml.XML.loadFile(xmlFile)
	val network = Network.fromXML(xml)
	val top = network.treatments.toList.sort((a, b) => a < b).first 

	def run() {
		val treeIterable = SpanningTreeEnumerator.treeEnumerator(
			network.treatmentGraph, top)
		var count = 0
		for (tree <- treeIterable) {
			count = count + 1
			val pmtz = new InconsistencyParametrization(network,
				new FundamentalGraphBasis(network.treatmentGraph, tree))
			val bsp = BaselineSearchProblem(pmtz)
			val sol = 
				(new DFS()).search(bsp) match {
					case Some(x) => true
					case None => false
				}
			println(count + ", " + sol + ", \"" + tree + "\"")
		}
	}
}

object EnumerateTest {
	def main(args: Array[String]) {
		val test = new EnumerateTestImpl(args(0))
		test.run()
	}
}

object CountTest {
	def main(args: Array[String]) {
		val xmlFile = args(0)
		val xml = scala.xml.XML.loadFile(xmlFile)
		val network = Network.fromXML(xml)
		val top = network.treatments.toList.sort((a, b) => a < b).first 

		val treeIterable = SpanningTreeEnumerator.treeEnumerator(
			network.treatmentGraph, top)
		var count = 0
		for (tree <- treeIterable) {
			count = count + 1
			println(count)
		}
	}
}

object SearchTest {
	def main(args: Array[String]) {
		val test = new SearchTestImpl(args(0))
		test.run()
	}
}

object StructurePrint {
	def main(args: Array[String]) {
		val xmlFile = args(0)
		val xml = scala.xml.XML.loadFile(xmlFile)
		val network = Network.fromXML(xml)
		println(network.treatmentGraph.dotString)
	}
}
