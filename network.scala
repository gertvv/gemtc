import org.drugis._

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
val inconsModel = new JagsSyntaxInconsistencyModel(model)

import java.io.PrintStream

val dataOut = new PrintStream(baseName + ".data")
dataOut.println(inconsModel.dataText)
dataOut.close()

val modelOut = new PrintStream(baseName + ".model")
modelOut.println(inconsModel.modelText)
modelOut.close()

val scriptOut = new PrintStream(baseName + ".script")
scriptOut.println(inconsModel.scriptText(baseName))
scriptOut.close()
