package org.drugis.mtc.util;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.drugis.mtc.Parameter;
import org.junit.Test;

public class MCMCResultsWriterTest {
	@Test
	public void acceptanceTest() throws IOException {
		FileResults r = new FileResults(MCMCResultsWriterTest.class.getResourceAsStream("writer-in.txt"), new Parameter[] { new FileResults.MyParameter("d.iPCI.mPCI"), new FileResults.MyParameter("var.d") }, 2, 2000);
		BufferedReader or = new BufferedReader(new InputStreamReader(MCMCResultsWriterTest.class.getResourceAsStream("writer-out.txt")));
		StringBuilder expected = new StringBuilder();
		while (or.ready()) {
			expected.append(or.readLine() + "\n");
		}
		or.close();
		
		StringWriter actual = new StringWriter();
		MCMCResultsWriter writer = new MCMCResultsWriter(r);
		r.makeSamplesAvailable();
		writer.write(actual);
		actual.close();
		assertEquals(expected.toString(), actual.toString());
	}
}
