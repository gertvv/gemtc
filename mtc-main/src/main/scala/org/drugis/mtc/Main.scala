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
import org.apache.commons.math.random.JDKRandomGenerator
import jargs.gnu.CmdLineParser

object ModelType extends Enumeration {
	type ModelType = Value
	val CONSISTENCY = Value("consistency")
	val INCONSISTENCY = Value("inconsistency")
}

import ModelType._

class Options(val xmlFile: String, val baseName: String,
	val modelType: ModelType, val scale: Double,
	val tuningIter: Int, val simulationIter: Int) {
}

class JAGSGenerator(options: Options) {
	def createConsistencyModel[M <: Measurement](
		network: Network[M],
		best: Tree[Treatment])
	: NetworkModel[M, ConsistencyParametrization[M]] = {
		ConsistencyNetworkModel(network, best)
	}

	def createInconsistencyModel[M <: Measurement](
		network: Network[M],
		best: Tree[Treatment])
	: NetworkModel[M, InconsistencyParametrization[M]] = {
		InconsistencyNetworkModel(network, best)
	}

	def createJagsModel[M <: Measurement](
		netw: Network[M], best: Tree[Treatment])
	: (JagsSyntaxModel[M, _], StartingValueGenerator[M]) =
		if (options.modelType == INCONSISTENCY) {
			val model = createInconsistencyModel(netw, best)
			(new JagsSyntaxModel(model),
			RandomizedStartingValueGenerator(model, new JDKRandomGenerator(), options.scale))
		} else {
			val model = createConsistencyModel(netw, best)
			(new JagsSyntaxModel(model),
			RandomizedStartingValueGenerator(model, new JDKRandomGenerator(), options.scale))
		}

	def generateModel[M <: Measurement](
			netw: Network[M], best: Tree[Treatment]) {
		val models = createJagsModel(netw, best)
		val syntaxModel = models._1 
		val initialGen = models._2

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

		val nChains = 4

		val scriptOut = new PrintStream(options.baseName + ".script")
		scriptOut.println(syntaxModel.scriptText(options.baseName, nChains, options.tuningIter, options.simulationIter))
		scriptOut.close()

		for (i <- 1 to nChains) {
			val paramOut = new PrintStream(options.baseName + ".param" + i)
			paramOut.println(syntaxModel.initialValuesText(initialGen))
			paramOut.close()
		}
	}

	def run() {
		val xml = scala.xml.XML.loadFile(options.xmlFile)
		val network = Network.fromXML(xml)
		val top = network.treatments.toList.sort((a, b) => a < b).first 
		println("Identifying spanning tree:")
		val best = network.bestSpanningTree(top)

		if (network.measurementType == classOf[DichotomousMeasurement]) {
			generateModel(network.asInstanceOf[Network[DichotomousMeasurement]], best)
		} else if (network.measurementType == classOf[ContinuousMeasurement]) {
			generateModel(network.asInstanceOf[Network[ContinuousMeasurement]], best)
		} else {
			println("Unsupported measurement type")
		}
	}
}

object Main {
	val usage = """Usage: java -jar ${MTC_JAR} [--type=consistency|inconsistency] [--scale=<f>] [--tuning=<n>] [--simulation=<m>]<xml-file> <output>
		|   When unspecified, the default is --type=consistency --scale=2.5
		|	 --tuning=30000 --simulation=20000.
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
		val parser = new CmdLineParser()
		val argType = parser.addStringOption("type")
		val argScale = parser.addDoubleOption("scale")
		val argTuning = parser.addIntegerOption("tuning")
		val argSimulation = parser.addIntegerOption("simulation")
		
		try {
			parser.parse(args)

			val modelType = ModelType.valueOf(
				parser.getOptionValue(argType, "consistency").asInstanceOf[String])
			val scale = parser.getOptionValue(argScale, 2.5).asInstanceOf[Double]
			val tuning = parser.getOptionValue(argTuning, 30000).asInstanceOf[Int]
			val simulation = parser.getOptionValue(argSimulation, 20000).asInstanceOf[Int]
			val otherArgs = parser.getRemainingArgs()

			if (otherArgs.length != 2) {
				System.err.println("2 non-option arguments expected, got " + otherArgs.length)
				None
			} else {
				modelType match {
					case None => None
					case Some(x) => Some(new Options(otherArgs(0), otherArgs(1), x, scale, tuning, simulation))
				}
			}
		} catch {
			case e: CmdLineParser.OptionException => {
				System.err.println(e.getMessage())
				None
			}
		}
	}
}
