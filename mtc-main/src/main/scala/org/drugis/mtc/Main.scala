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
	val consistency = Value("consistency")
	val inconsistency = Value("inconsistency")
	val nodesplit = Value("nodesplit")
}

import ModelType._

class Options(val xmlFile: String, val baseName: String,
	val modelType: ModelType, val scale: Double,
	val tuningIter: Int, val simulationIter: Int) {
}

class ModelSpecification[M <: Measurement](
	val model: JagsSyntaxModel[M, _],
	val generator: StartingValueGenerator[M],
	val nameSuffix: String) {
}

class JAGSGenerator(options: Options) {
	def createJagsModel[M <: Measurement, P <: Parametrization[M]](
		model: NetworkModel[M, P], suffix: String)
	: ModelSpecification[M] = {
		new ModelSpecification(new JagsSyntaxModel(model),
			RandomizedStartingValueGenerator(model,
				new JDKRandomGenerator(), options.scale), suffix)
	}

	def createJagsModels[M <: Measurement](netw: Network[M])
	: List[ModelSpecification[M]] = {
		options.modelType match {
			case ModelType.consistency => {
				val model = ConsistencyNetworkModel(netw)
				List(createJagsModel(model, ".cons"))
			}
			case ModelType.inconsistency => {
				val model = InconsistencyNetworkModel(netw)
				List(createJagsModel(model, ".inco"))
			}
			case ModelType.nodesplit => {
				NodeSplitNetworkModel.getSplittableNodes(netw) map (n => {
					val model = NodeSplitNetworkModel(netw, n)
					createJagsModel(model, ".splt." + n._1.id + "." + n._2.id)
				}) toList
			}
		}
	}

	def writeModel[M <: Measurement](spec: ModelSpecification[M]) {
		println("Identified spanning tree:")
		println("\tgraph {")
		for (e <- spec.model.model.basis.tree.edgeSet) {
			println("\t\t" + e._1.id + " -- " + e._2.id)
		}
		println("\t}")

		println("Writing JAGS scripts: " + options.baseName + spec.nameSuffix + ".*")

		val dataOut = new PrintStream(options.baseName + spec.nameSuffix + ".data")
		dataOut.println(spec.model.dataText)
		dataOut.close()

		val modelOut = new PrintStream(options.baseName + spec.nameSuffix + ".model")
		modelOut.println(spec.model.modelText)
		modelOut.close()

		val nChains = 4

		val scriptOut = new PrintStream(options.baseName + spec.nameSuffix + ".script")
		scriptOut.println(spec.model.scriptText(options.baseName + spec.nameSuffix, nChains, options.tuningIter, options.simulationIter))
		scriptOut.close()

		for (i <- 1 to nChains) {
			val paramOut = new PrintStream(options.baseName + spec.nameSuffix + ".param" + i)
			paramOut.println(spec.model.initialValuesText(spec.generator))
			paramOut.close()
		}

		def writeAnalysis(model: JagsSyntaxModel[M, _]) {
			val analysisOut = new PrintStream(options.baseName + spec.nameSuffix + ".analysis.R")
			analysisOut.println(spec.model.analysisText(options.baseName + spec.nameSuffix))
			analysisOut.close()
		}

		options.modelType match {
			case ModelType.consistency => writeAnalysis(spec.model)
			case ModelType.inconsistency => writeAnalysis(spec.model)
			case _ =>
		}
	}

	def generateModel[M <: Measurement](netw: Network[M]) {
		for (spec <- createJagsModels(netw)) {
			writeModel(spec)
		}
	}

	def run() {
		val xml = scala.xml.XML.loadFile(options.xmlFile)
		val network = Network.fromXML(xml)

		if (network.measurementType == classOf[DichotomousMeasurement]) {
			generateModel(network.asInstanceOf[Network[DichotomousMeasurement]])
		} else if (network.measurementType == classOf[ContinuousMeasurement]) {
			generateModel(network.asInstanceOf[Network[ContinuousMeasurement]])
		} else {
			println("Unsupported measurement type")
		}
	}
}

object Main {
	val usage = """
		|Usage: java -jar ${MTC_JAR} \
		|      [--type=consistency|inconsistency|nodesplit] \
		|      [--scale=<f>] [--tuning=<n>] [--simulation=<m>] \
		|      <xmlfile> [<output>]
		|When unspecified, the default is --type=consistency --scale=2.5
		|   --tuning=30000 --simulation=20000 <xmlfile> ${<xmlfile>%.xml}.
		|
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

			if (otherArgs.length < 1 || otherArgs.length > 2) {
				System.err.println("1 or 2 non-option arguments expected, got " + otherArgs.length)
				None
			} else {
				val xmlFile = otherArgs(0)
				val baseName = {
					if (otherArgs.length == 2) otherArgs(1)
					else xmlFile.stripSuffix(".xml")
				}
				modelType match {
					case None => None
					case Some(x) => Some(new Options(xmlFile, baseName, x, scale, tuning, simulation))
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
