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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.drugis.mtc.Parameter;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.parameterization.RandomEffectsStandardDeviation;
import org.drugis.mtc.test.FileResults;
import org.drugis.mtc.util.EmpiricalDensityDataset.PlotParameter;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.junit.Before;
import org.junit.Test;

public class EmpiricalDensityDatasetTest {
	/*
	 * Single density test case generated in R using:
		x <- read.table("conv-samples.txt", sep=",")
		chains <- sapply(0:2, function(i) { x[(5001 + i * 10000):((i+1)*10000),1] })
		y <- c(chains)
		q <- quantile(y, c(0.025, 0.975), type=6)
		nBins <- 50
		binSize <- (q[2] - q[1])/nBins
		breaks <- c(min(y), seq(from=q[1], to=q[2], by=binSize), max(y))
		dens <- hist(y, plot=F, breaks=breaks)$density[2:51]
		write.table(dens, "density-single.txt")
	 */
	/*
	 * Multiple density test case generated in R using:
		x <- read.table("conv-samples.txt", sep=",")
		readParam <- function(j) {
			chains <- sapply(0:2, function(i) { x[(5001 + i * 10000):((i+1)*10000),j] })
			c(chains)
		}
		q <- sapply(1:3, function(j) { quantile(readParam(j), c(0.025, 0.975), type=6) })
		q <- c(min(q), max(q))
		nBins <- 50
		binSize <- (q[2] - q[1])/nBins
		y <- c(sapply(1:3, function(j) { readParam(j) }))
		breaks <- c(min(y), seq(from=q[1], to=q[2], by=binSize), max(y))
		dens <- sapply(1:3, function(j) { hist(readParam(j), plot=F, breaks=breaks)$density[2:51] })
		write.table(dens, "density-multiple.txt")
	 */

	private static final int N_BINS = 50;
	private static final double EPSILON = 0.0000001;
	private Parameter[] d_parameters;
	private FileResults d_results;

	private static final int[] s_counts1 = {54, 73, 82, 87,  93, 110, 140, 155, 177, 183, 238, 231, 250,
			254, 280, 300, 359, 371, 426, 401, 435, 440, 477, 504, 474, 450, 453, 478, 506, 475, 459,
			462, 411, 412, 382, 336, 328, 318, 359, 272, 270, 224, 200, 180, 126, 149, 161,  93,  83,  69};
	private static final double[] s_quantiles1 = { 0.1472530, 0.7713364 };
	private static final String FILE_SINGLE_DENS = "density-single.txt";
	private static final String FILE_MULTI_DENS = "density-multiple.txt";

	@Before
	public void setUp() throws IOException {
		Treatment t1 = new Treatment("iPCI");
		Treatment t2 = new Treatment("mPCI");
		Treatment t3 = new Treatment("sPCI");
		d_parameters = new Parameter[] {
				new BasicParameter(t1, t2), new BasicParameter(t2, t3), new RandomEffectsStandardDeviation()
		};
		d_results = readSamples();
	}

	@Test
	public void testReadFiles() throws IOException {
		assertNotNull(readDensity(FILE_SINGLE_DENS, 0));
		assertNotNull(readDensity(FILE_MULTI_DENS, 0));
		assertNotNull(readDensity(FILE_MULTI_DENS, 1));
		assertNotNull(readDensity(FILE_MULTI_DENS, 2));
	}

	@Test
	public void testInitialize() {
		EmpiricalDensityDataset sdd = new EmpiricalDensityDataset(N_BINS, d_results, d_parameters[0]);
		assertEquals(1, sdd.getSeriesCount());
		assertEquals(N_BINS, sdd.getItemCount(0));
	}

	@Test
	public void testCountLength() {
		d_results.makeSamplesAvailable();
		EmpiricalDensityDataset edd = new EmpiricalDensityDataset(N_BINS, d_results, d_parameters[0]);
		assertEquals(N_BINS, edd.getCounts(0).length);
	}

	@Test
	public void testCounts() {
		d_results.makeSamplesAvailable();
		EmpiricalDensityDataset edd = new EmpiricalDensityDataset(N_BINS, d_results, d_parameters[0]);
		assertArrayEquals(s_counts1, edd.getCounts(0));
	}

	@Test
	public void testDensities() throws IOException {
		d_results.makeSamplesAvailable();
		EmpiricalDensityDataset edd = new EmpiricalDensityDataset(N_BINS, d_results, d_parameters[0]);
		double[] normDensities = readDensity(FILE_SINGLE_DENS, 0);
		assertArrayEquals(normDensities, edd.getDensities(0), EPSILON);
	}

	@Test
	public void testDensitiesDynamic() throws IOException {
		EmpiricalDensityDataset edd = new EmpiricalDensityDataset(N_BINS, d_results, d_parameters[0]);
		d_results.makeSamplesAvailable();
		double[] normDensities = readDensity(FILE_SINGLE_DENS, 0);
		assertArrayEquals(normDensities, edd.getDensities(0), EPSILON);
	}

	@Test
	public void testResultsEventShouldTriggerDatasetChanged() {
		EmpiricalDensityDataset edd = new EmpiricalDensityDataset(N_BINS, d_results, d_parameters[0]);
		final List<DatasetChangeEvent> list = new ArrayList<DatasetChangeEvent>();
		edd.addChangeListener(new DatasetChangeListener() {
			public void datasetChanged(final DatasetChangeEvent e) {
				list.add(e);
			}
		});
		d_results.makeSamplesAvailable();

		assertEquals(1, list.size());
	}

	@Test
	public void testGetX() throws IOException {
		d_results.makeSamplesAvailable();
		EmpiricalDensityDataset edd = new EmpiricalDensityDataset(N_BINS, d_results, d_parameters[0]);
		double bottom = s_quantiles1[0];
		double top = s_quantiles1[1];
		double interval = (top - bottom) / N_BINS;

		assertEquals((0.5 + 1) * interval + bottom, edd.getX(0, 1), EPSILON);
		assertEquals((0.5 + 25) * interval + bottom, edd.getX(0, 25), EPSILON);
		assertEquals((0.5 + 49) * interval + bottom, edd.getX(0, 49), EPSILON);
	}

	@Test
	public void testInitializeWithMultipleDensities() throws IOException {
		FileResults results = readSamples();
		results.makeSamplesAvailable();
		d_results.makeSamplesAvailable();
		EmpiricalDensityDataset edd = new EmpiricalDensityDataset(N_BINS, new PlotParameter(d_results, d_parameters[0]),
				new PlotParameter(d_results, d_parameters[1]), new PlotParameter(results , d_parameters[2]));
		assertEquals(3, edd.getSeriesCount());
		assertEquals(N_BINS, edd.getItemCount(0));
		assertEquals(N_BINS, edd.getItemCount(1));
		assertEquals(N_BINS, edd.getItemCount(2));
		assertEquals(d_parameters[0].getName(), edd.getSeriesKey(0));
		assertEquals(d_parameters[1].getName(), edd.getSeriesKey(1));

		assertEquals(-1.5985012, edd.getLowerBound(), EPSILON);
		assertEquals(0.7713364, edd.getUpperBound(), EPSILON);
	}

	@Test
	public void testMultiDensityCounts() {
		d_results.makeSamplesAvailable();
		EmpiricalDensityDataset edd = new EmpiricalDensityDataset(N_BINS, new PlotParameter(d_results, d_parameters[0]),
				new PlotParameter(d_results, d_parameters[0]));
		assertArrayEquals(s_counts1, edd.getCounts(0));
	}

	@Test
	public void testMultipleDensities() throws IOException {
		FileResults results = readSamples();
		results.makeSamplesAvailable();
		d_results.makeSamplesAvailable();
		EmpiricalDensityDataset edd = new EmpiricalDensityDataset(N_BINS, new PlotParameter(d_results, d_parameters[0]),
				new PlotParameter(d_results, d_parameters[1]), new PlotParameter(results , d_parameters[2]));
		double[] normDensities0 = readDensity(FILE_MULTI_DENS, 0);
		assertArrayEquals(normDensities0, edd.getDensities(0), EPSILON);
		double[] normDensities1 = readDensity(FILE_MULTI_DENS, 1);
		assertArrayEquals(normDensities1, edd.getDensities(1), EPSILON);
		double[] normDensities2 = readDensity(FILE_MULTI_DENS, 2);
		assertArrayEquals(normDensities2, edd.getDensities(2), EPSILON);
		assertEquals(normDensities0[1], edd.getY(0, 1), EPSILON);
		assertEquals(normDensities1[6], edd.getY(1, 6), EPSILON);
	}

	@Test
	public void testMDNoneInitialized() throws IOException {
		FileResults results = readSamples();
		new EmpiricalDensityDataset(N_BINS, new PlotParameter(d_results, d_parameters[0]),
				new PlotParameter(d_results, d_parameters[1]), new PlotParameter(results , d_parameters[2]));
	}

	@Test
	public void testMDSomeInitialized() throws IOException {
		FileResults results = readSamples();
		d_results.makeSamplesAvailable();
		EmpiricalDensityDataset edd = new EmpiricalDensityDataset(N_BINS, new PlotParameter(d_results, d_parameters[0]),
				new PlotParameter(d_results, d_parameters[1]), new PlotParameter(results , d_parameters[2]));

		// The range of the last parameter is completely within the range of the others.
		double[] normDensities0 = readDensity(FILE_MULTI_DENS, 0);
		assertArrayEquals(normDensities0, edd.getDensities(0), EPSILON);
		double[] normDensities1 = readDensity(FILE_MULTI_DENS, 1);
		assertArrayEquals(normDensities1, edd.getDensities(1), EPSILON);
		double[] normDensities2 = new double[N_BINS];
		assertArrayEquals(normDensities2, edd.getDensities(2), EPSILON);
	}

	@Test
	public void testOneResultsOneEvent() {
		EmpiricalDensityDataset edd = new EmpiricalDensityDataset(N_BINS, new PlotParameter(d_results, d_parameters[0]),
				new PlotParameter(d_results, d_parameters[1]), new PlotParameter(d_results , d_parameters[2]));
		final List<DatasetChangeEvent> list = new ArrayList<DatasetChangeEvent>();
		edd.addChangeListener(new DatasetChangeListener() {
			public void datasetChanged(final DatasetChangeEvent e) {
				list.add(e);
			}
		});
		d_results.makeSamplesAvailable();

		assertEquals(1, list.size());
	}

	private FileResults readSamples() throws IOException {
		InputStream is = EmpiricalDensityDatasetTest.class.getResourceAsStream("conv-samples.txt");
		FileResults results = new FileResults(is, d_parameters, 3, 10000);
		is.close();
		return results;
	}

	private double[] readDensity(final String file, final int param) throws IOException {

		InputStream is = EmpiricalDensityDatasetTest.class.getResourceAsStream(file);
		double[] data = new double[N_BINS];

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		reader.readLine(); // skip the first line
		for (int i = 0; reader.ready(); ++i) {
			String line = reader.readLine();
			if (line == null) break;
			StringTokenizer tok = new StringTokenizer(line, " ");
			for (int p = -1; p < param; ++p) {
				tok.nextToken(); // skip the first column (IDs) + every param before the one of interest
			}
			data[i] = Double.parseDouble(tok.nextToken());
		}
		reader.close();
		return data;
	}

}
