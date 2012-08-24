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

package org.drugis.mtc.util;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.drugis.mtc.Parameter;
import org.drugis.mtc.test.FileResults;
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
