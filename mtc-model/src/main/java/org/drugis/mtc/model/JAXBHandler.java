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

import java.io.InputStream;
import java.io.InputStreamReader;
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


	public static Network readNetwork(InputStream inputStream) throws JAXBException {
		return readNetwork(new InputStreamReader(inputStream));
	}

	public static void writeNetwork(Network data, OutputStream out) throws JAXBException {
		// Since there is no way to restrict the allowed attributes based on values elsewhere in the XML,
		// enforce the restrictions before marshalling.
		createMarshaller().marshal(data.restrictMeasurements(), out);
	}

	private static Marshaller createMarshaller() throws JAXBException {
		Marshaller marshaller = getContext().createMarshaller();
        marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
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
