package org.drugis.mtc.model;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.drugis.mtc.data.NetworkData;
import org.drugis.mtc.data.ObjectFactory;
import org.junit.Test;

public class UnmarshallTest {

	@Test
	public void testBla() throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
		Unmarshaller u = context.createUnmarshaller();
		NetworkData data = (NetworkData)u.unmarshal(new StringReader("<?xml version=\"1.0\"?><network><treatments><treatment id=\"A\">Argh!</treatment></treatments><studies></studies></network>"));
		System.out.println(data);
		System.out.println(data.getTreatmentList().getTreatment().get(0));
	}
}
