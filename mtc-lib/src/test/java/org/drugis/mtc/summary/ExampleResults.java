package org.drugis.mtc.summary;

import java.io.IOException;

import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.util.FileResults;

public class ExampleResults extends FileResults implements MCMCResults {
	private static class MyParameter implements Parameter {
		private String d_name;
		public MyParameter(String name) { d_name = name; }
		public String getName() { return d_name; }
	}
	
	public static final int N_SAMPLES = 500;
	public static final int N_CHAINS = 2;

	public ExampleResults() throws IOException {
		super(ExampleResults.class.getResourceAsStream("samples.txt"), 
				new Parameter[] { new MyParameter("x"), new MyParameter("y"), new MyParameter("s") },
				N_CHAINS, N_SAMPLES);
	}
}
