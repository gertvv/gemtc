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
	val usage = """Usage: java -jar ${MTC_JAR} [--consistency|--inconsistency] <xml-file> <output>
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
