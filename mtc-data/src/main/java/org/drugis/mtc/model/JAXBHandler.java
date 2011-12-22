package org.drugis.mtc.model;

import java.io.OutputStream;
import java.io.Reader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.drugis.mtc.data.NetworkData;
import org.drugis.mtc.data.ObjectFactory;

public class JAXBHandler {
	private static JAXBContext s_context;
	
	public static Network readNetwork(Reader reader) throws JAXBException {
		// Since JAXB does not access the root element through the ObjectFactory, 
		// we have to work around it like this.
		return new Network((NetworkData)createUnmarshaller().unmarshal(reader));
	}

	public static void writeNetwork(Network data, OutputStream out) throws JAXBException {
		// Since there is no way to restrict the allowed attributes based on values elsewhere in the XML,
		// enforce the restrictions before marshalling.
		createMarshaller().marshal(data.restrictMeasurements(), out);
	}
	
	private static Marshaller createMarshaller() throws JAXBException {
		Marshaller marshaller = getContext().createMarshaller();
        marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
        return marshaller;
	}
	
	private static Unmarshaller createUnmarshaller() throws JAXBException {
		return getContext().createUnmarshaller();
	}

	private static JAXBContext getContext() throws JAXBException {
		if (s_context == null) {
			s_context = JAXBContext.newInstance(ObjectFactory.class);
		}
		return s_context;
	}
}
