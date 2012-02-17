/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
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

package org.drugis.mtc.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.drugis.mtc.MCMCResults;

public class MCMCResultsWriter {

	private MCMCResults d_results;

	public MCMCResultsWriter(MCMCResults r) {
		d_results = r;
	}

	public void write(OutputStream o) throws IOException {
		write(new OutputStreamWriter(o));
	}
	
	public void write(Writer w) throws IOException {
		BufferedWriter out = new BufferedWriter(w);
		out.write("`trace` <-\n");
		out.write("structure(list(");
		String paramstr = "";
		for(int p = 0; p < d_results.getParameters().length; ++ p) {
			String namestr = "\"" + d_results.getParameters()[p].getName() + "\"";
			paramstr += namestr + ", ";
			out.write(namestr + " = structure(c(");
			for (int i = 0; i < d_results.getNumberOfChains(); ++i) {
				for (int j = 0; j < d_results.getNumberOfSamples(); ++j) {
					Double d = d_results.getSample(p, i, j);
					out.write(d.toString());
					if (j != d_results.getNumberOfSamples() - 1 || i != d_results.getNumberOfChains() - 1) out.write(",");
				}
			}
			out.write("), .Dim = structure(c(1L," + d_results.getNumberOfSamples() + "L," + d_results.getNumberOfChains() + "L), ");
			out.write(".Names = c(\"\",\"iteration\",\"chain\")))");
			if (p < d_results.getParameters().length - 1) out.write(", \n");
		}
		paramstr = paramstr.substring(0, paramstr.length()-2);
		out.write("), \n.Names = c(" + paramstr + "))\n");
		out.flush();
	}
}
