/*
 * This file is part of the GeMTC software for MTC model generation and
 * analysis. GeMTC is distributed from http://drugis.org/gemtc.
 * Copyright (C) 2009-2012 Gert van Valkenhoef.
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

package org.drugis.mtc;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.Option;

public class Main {
	private static final String usage =
		"Usage: java -jar ${MTC_JAR} \\\n" +
		"      [--type=consistency|inconsistency|nodesplit] \\\n" +
		"      [--scale=<f>] [--tuning=<n>] [--simulation=<m>] \\\n" +
		"      [--bugs] [--suppress] \\\n" +
		"      <datafile> [<output>]\n" +
		"When unspecified, the default is --type=consistency --scale=2.5\n" +
		"   --tuning=20000 --simulation=40000 <datafile> ${<datafile>%.gemtc}.\n" +
		"\n" +
		"This will generate a JAGS or BUGS model from the specified GeMTC\n" +
		"datafile.";

	public static void main(String[] args) {
		Options options = parseArguments(args);
		if (options != null) {
			try {
				new JagsGenerator(options).run();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(2);
			}
		} else {
			System.err.println(usage);
			System.exit(1);
		}
	}

	public static Options parseArguments(String[] args) {
		CmdLineParser parser = new CmdLineParser();
		Option argType = parser.addStringOption("type");
		Option argScale = parser.addDoubleOption("scale");
		Option argTuning = parser.addIntegerOption("tuning");
		Option argSimulation = parser.addIntegerOption("simulation");
		Option argSuppress = parser.addBooleanOption("suppress");
		Option argBugs = parser.addBooleanOption("bugs");
		
		try {
			parser.parse(args);

			ModelType modelType = ModelType.valueOf(((String)parser.getOptionValue(argType, "consistency")).toUpperCase());
			Double scale = (Double)parser.getOptionValue(argScale, 2.5);
			Integer tuning = (Integer)parser.getOptionValue(argTuning, 20000);
			Integer simulation = (Integer)parser.getOptionValue(argSimulation, 40000);
			Boolean suppress = (Boolean)parser.getOptionValue(argSuppress, false);
			Boolean bugs = (Boolean)parser.getOptionValue(argBugs, false);
			String[] otherArgs = parser.getRemainingArgs();

			if (otherArgs.length < 1 || otherArgs.length > 2) {
				System.err.println("1 or 2 non-option arguments expected, got " + otherArgs.length);
				return null;
			} else {
				String xmlFile = otherArgs[0];
				String baseName = otherArgs.length == 2 ? otherArgs[1] : xmlFile.replaceAll(".gemtc$", "");
				if (modelType == null) {
					return null;
				}
				return new Options(xmlFile, baseName, modelType, scale, tuning, simulation, suppress, bugs);
			}
		} catch (CmdLineParser.OptionException e) {
			System.err.println(e.getMessage());
			return null;
		}
	}
}
