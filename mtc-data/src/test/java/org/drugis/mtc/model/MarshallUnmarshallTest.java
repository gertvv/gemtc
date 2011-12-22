package org.drugis.mtc.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.StringReader;

import javax.xml.bind.JAXBException;

import org.drugis.mtc.data.DataType;
import org.junit.Test;

public class MarshallUnmarshallTest {
	
	@Test
	public void testEmpty() throws JAXBException {
		Network data = JAXBHandler.readNetwork(new StringReader("<?xml version=\"1.0\"?><network><treatments></treatments><studies></studies></network>"));
		assertNull(data.getDescription());
		assertEquals(DataType.RATE, data.getType());
		assertEquals(0, data.getTreatments().size());
		assertEquals(0, data.getStudies().size());
		JAXBHandler.writeNetwork(data, System.out);
	}

	@Test
	public void testTreatment() throws JAXBException {
		Network data = JAXBHandler.readNetwork(
				new StringReader("<?xml version=\"1.0\"?><network><treatments><treatment id=\"A\">Argh!</treatment></treatments><studies></studies></network>"));
		System.out.println(data);
		System.out.println(data.getTreatmentList().getTreatment().get(0));
	}
}
