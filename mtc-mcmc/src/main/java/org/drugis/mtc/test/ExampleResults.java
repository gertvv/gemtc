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

package org.drugis.mtc.test;

import java.io.IOException;

import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.Parameter;
import org.drugis.mtc.test.FileResults;

public class ExampleResults extends FileResults implements MCMCResults {
	public static final int N_SAMPLES = 500;
	public static final int N_CHAINS = 2;

	public ExampleResults() throws IOException {
		super(ExampleResults.class.getResourceAsStream("samples.txt"), 
				new Parameter[] { new MyParameter("x"), new MyParameter("y"), new MyParameter("s") },
				N_CHAINS, N_SAMPLES);
	}
}
