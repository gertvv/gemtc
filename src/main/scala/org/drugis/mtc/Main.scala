package org.drugis.mtc

import org.drugis.mtc.jags._
import java.io.PrintStream

class Options(val xmlFile: String, val baseName: String,
	val isInconsistency: Boolean) {
}

class JAGSGenerator(options: Options) {
	def run() {
		val xml = scala.xml.XML.loadFile(options.xmlFile)
		val network = Network.fromXML(xml)
		val top = network.treatments.toList.sort((a, b) => a < b).first 
		println("Identifying spanning tree:")
		val best = network.bestSpanningTree(top)
		val model = NetworkModel(network, best)

		val syntaxModel = {
			if (options.isInconsistency)
				new JagsSyntaxInconsistencyModel(model)
			else
				new JagsSyntaxConsistencyModel(model)
		}

		println("\tgraph {")
		for (e <- best.edgeSet) {
			println("\t\t" + e._1.id + " -- " + e._2.id)
		}
		println("\t}")

		println("Writing JAGS scripts: " + options.baseName + ".*")

		val dataOut = new PrintStream(options.baseName + ".data")
		dataOut.println(syntaxModel.dataText)
		dataOut.close()

		val modelOut = new PrintStream(options.baseName + ".model")
		modelOut.println(syntaxModel.modelText)
		modelOut.close()

		val scriptOut = new PrintStream(options.baseName + ".script")
		scriptOut.println(syntaxModel.scriptText(options.baseName))
		scriptOut.close()

		val analysisOut = new PrintStream(options.baseName + ".analysis.R")
		analysisOut.println(syntaxModel.analysisText(options.baseName))
		analysisOut.close()
	}
}

object Main {
	val usage = """Usage: java -cp ${MTC_JAR} \\
		|      org.drugis.mtc.Main [--consistency|--inconsistency] <xml-file> <output>
		|   When unspecified, the default is --inconsistency.
		|This will generate a JAGS model from the specified XML file, which can
		|subsequently be run using the output.script file.""".stripMargin

	def main(args: Array[String]) {
		val opts = parseArguments(args)
		opts match {
			case Some(x) => (new JAGSGenerator(x)).run()
			case None => println(usage); exit(1)
		}
	}

	def parseArguments(args: Array[String]): Option[Options] = {
		if (args.size == 2) {
			Some(new Options(args(0), args(1), true))
		} else if (args.size == 3) {
			if (args(0) == "--consistency") {
				Some(new Options(args(1), args(2), false))
			} else if (args(0) == "--inconsistency") {
				Some(new Options(args(1), args(2), true))
			} else {
				None
			}
		} else {
			None
		}
	}
}
