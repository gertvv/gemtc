import org.drugis.mtc._
import org.drugis.mtc.jags._
import org.drugis.mtc.yadas._

if (args.size != 1) {
	println("Usage: network.scala <data-xml-file>")
	exit()
}

val xmlFile = args(0)
val baseName =
	if (xmlFile.length > 4 &&
			xmlFile.substring(xmlFile.length - 4) == ".xml") 
		xmlFile.substring(0, xmlFile.length - 4)
	else xmlFile

val xml = scala.xml.XML.loadFile(args(0))

val network = Network.fromXML(xml)

val top = network.treatments.toList.sort((a, b) => a < b).first 

val best = network.bestSpanningTree(top)

println("graph {")
for (e <- best.edgeSet) {
	println(e._1.id + " -- " + e._2.id)
}
println("}")

/*
val iterable = SpanningTreeEnumerator.treeEnumerator(network.treatmentGraph)
var i = 0
for (t <- iterable) {
	i += 1
}
println("Generated " + i + " trees")
*/
val model = NetworkModel(network, best)
val syntaxModel = new JagsSyntaxInconsistencyModel(model)

import java.io.PrintStream

println("Writing JAGS scripts: " + baseName + ".*")

val dataOut = new PrintStream(baseName + ".data")
dataOut.println(syntaxModel.dataText)
dataOut.close()

val modelOut = new PrintStream(baseName + ".model")
modelOut.println(syntaxModel.modelText)
modelOut.close()

val scriptOut = new PrintStream(baseName + ".script")
scriptOut.println(syntaxModel.scriptText(baseName))
scriptOut.close()

val analysisOut = new PrintStream(baseName + ".analysis.R")
analysisOut.println(syntaxModel.analysisText(baseName))
analysisOut.close()

class ListenerImpl extends ProgressListener {
	def update(mtc: MixedTreatmentComparison, evt: ProgressEvent) {
		println(evt)
	}
}

println("Running YADAS inconsistency model: ")
val inconsModel = (new YadasModelFactory()).getInconsistencyModel(network)
inconsModel.addProgressListener(new ListenerImpl())
inconsModel.run()
val treatments = network.treatments.toList.sort((a, b) => a < b)
for (i <- 0 until (treatments.size - 1); j <- (i + 1) until treatments.size) {
	println(treatments(i).id + " " + treatments(j).id + " " +
		inconsModel.getRelativeEffect(treatments(i), treatments(j)))
}
for (f <- inconsModel.getInconsistencyFactors().toArray(Array[InconsistencyParameter]())) {
	println(f + " " + inconsModel.getInconsistency(f))
}

println("Running YADAS consistency model: ")
	val consModel = (new YadasModelFactory()).getConsistencyModel(network)
	consModel.addProgressListener(new ListenerImpl())
	consModel.run()
	for (i <- 0 until (treatments.size - 1); j <- (i + 1) until treatments.size) {
		println(treatments(i).id + " " + treatments(j).id + " " +
			consModel.getRelativeEffect(treatments(i), treatments(j)))
}
println()
for (t <- treatments) {
	for (i <- 1 to treatments.size) {
		println(t.id + " " + i + " " + consModel.rankProbability(t, i))
	}
}
