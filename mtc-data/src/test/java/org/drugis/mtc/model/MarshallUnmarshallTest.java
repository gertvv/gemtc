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
