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
		val model = NetworkModel(network, best)
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
