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

package org.drugis.mtc.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.bind.JAXBException;

import org.junit.Test;

public class MarshallUnmarshallTest {
	
	@Test
	public void testEmpty() throws JAXBException, IOException {
		Network data = JAXBHandler.readNetwork(new StringReader("<?xml version=\"1.0\"?><network><treatments></treatments><studies></studies></network>"));
		Network expected = new Network();
		assertEquals(expected, data);
		
		// Round trip
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		JAXBHandler.writeNetwork(data, bos);
		bos.close();
		assertEquals(data, JAXBHandler.readNetwork(new ByteArrayInputStream(bos.toByteArray())));
	}

	@Test
	public void testTreatment() throws JAXBException {
		Network data = JAXBHandler.readNetwork(
				new StringReader("<?xml version=\"1.0\"?><network><treatments><treatment id=\"A\">Argh!</treatment></treatments><studies></studies></network>"));
		
		Network expected = new Network();
		expected.getTreatments().add(new Treatment("A", "Argh!"));
		
		assertEquals(expected, data);
	}
	
	@Test
	public void testStudy() throws JAXBException {
		Network data = JAXBHandler.readNetwork(
				new StringReader("<?xml version=\"1.0\"?><network><treatments/><studies><study id=\"Study 1\"/></studies></network>"));
		
		Network expected = new Network();
		expected.getStudies().add(new Study("Study 1"));
		
		assertEquals(expected, data);
	}
	
	@Test
	public void testTreatmentRef() throws JAXBException {
		Network data = JAXBHandler.readNetwork(
				new StringReader("<?xml version=\"1.0\"?><network><treatments><treatment id=\"A\">Argh!</treatment></treatments><studies><study id=\"Study 1\"><measurement treatment=\"A\"/></study></studies></network>"));
		assertSame(data.getTreatments().get(0), data.getStudies().get(0).getMeasurements().get(0).getTreatment());
	}
}
