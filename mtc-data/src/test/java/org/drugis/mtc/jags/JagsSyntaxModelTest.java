package org.drugis.mtc.jags;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.drugis.mtc.model.JAXBHandler;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.parameterization.InconsistencyParameterization;
import org.drugis.mtc.yadas.ContinuousDataIT;
import org.junit.Test;

import sun.misc.IOUtils;

public class JagsSyntaxModelTest {
	@Test
	public void testWriteInt() {
		assertEquals("3L", JagsSyntaxModel.writeNumber(3, true));
		assertEquals("15L", JagsSyntaxModel.writeNumber(15, true));
	}

	@Test
	public void testWriteFloat() {
		assertEquals("3.0", JagsSyntaxModel.writeNumber(3.0, true));
		assertEquals("15.0", JagsSyntaxModel.writeNumber(15.0, true));
	}

	@Test 
	public void testIntMatrixColMajor() {
		Integer m[][] = { {1, 2, 3, 4}, {5, 6, 7, 8} };
		assertEquals("structure(c(1L, 5L, 2L, 6L, 3L, 7L, 4L, 8L), .Dim = c(2L, 4L))", JagsSyntaxModel.writeMatrix(m, true));
	}
	
	@Test 
	public void testIntMatrixRowMajor() {
		Integer m[][] = { {1, 2, 3, 4}, {5, 6, 7, 8} };
		assertEquals("structure(.Data = c(1, 2, 3, 4, 5, 6, 7, 8), .Dim = c(2, 4))", JagsSyntaxModel.writeMatrix(m, false));
	}
	
	private static String read(String path) throws IOException {
		InputStream is = JagsSyntaxModelTest.class.getResourceAsStream(path);
		StringBuilder str = new StringBuilder();
		Reader reader = new InputStreamReader(is, "UTF-8");
		char[] buffer = new char[2048];
		int read;
		while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
			str.append(buffer, 0, read);
		}
		reader.close();
		return str.toString();
	}
	
	@Test
	public void testDichotomousInconsistency() throws JAXBException, IOException {
		InputStream is = JagsSyntaxModelTest.class.getResourceAsStream("network1.xml");
		Network network = JAXBHandler.readNetwork(is);
		
		JagsSyntaxModel model = new JagsSyntaxModel(network, InconsistencyParameterization.create(network), true);
		
		assertEquals(read("data-inco-dich.txt"), model.dataText());
		assertEquals(read("model-inco-dich.txt"), model.modelText());
	}
}
