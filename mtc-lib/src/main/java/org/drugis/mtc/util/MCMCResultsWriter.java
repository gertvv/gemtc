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
