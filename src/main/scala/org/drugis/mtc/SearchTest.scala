package org.drugis.mtc

class SearchTestImpl(xmlFile: String) {
	val xml = scala.xml.XML.loadFile(xmlFile)
	val network = Network.fromXML(xml)
	val top = network.treatments.toList.sort((a, b) => a < b).first 

	def run() {
		val t0 = System.currentTimeMillis()
		val result = network.searchSpanningTree(top)
		val best = result._1
		val model = NetworkModel(network, best)
		val t1 = System.currentTimeMillis()
		println("Enumerated " + result._2)
		println("ICDF " + model.inconsistencyParameters.size)
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
