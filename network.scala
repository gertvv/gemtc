import org.drugis.mtc._
import org.drugis.mtc.jags._

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

println("Running JAGS via JNI: ")
val jniModel = (new JagsModelFactory()).getInconsistencyModel(network)
jniModel.addProgressListener(new ListenerImpl())
jniModel.run()
val treatments = network.treatments.toList.sort((a, b) => a < b)
for (i <- 0 until (treatments.size - 1); j <- (i + 1) until treatments.size) {
	println(treatments(i).id + " " + treatments(j).id + " " +
		jniModel.getRelativeEffect(treatments(i), treatments(j)))
}
for (f <- jniModel.getInconsistencyFactors().toArray(Array[InconsistencyParameter]())) {
	println(f + " " + jniModel.getInconsistency(f))
}
