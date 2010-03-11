package org.drugis.mtc;

import org.junit.Test;
import static org.junit.Assert.*;

public class DefaultModelFactoryTest {
	private static final String YADAS =
		"org.drugis.mtc.yadas.YadasModelFactory";

	@Test public void testDefaultInstance() throws Exception {
		ModelFactory fac = DefaultModelFactory.instance();
		assertEquals(Class.forName(YADAS), fac.getClass());
	}

	@Test public void testOtherInstance() throws Exception {
		ModelFactory fac = DefaultModelFactory.instance(YADAS);
		assertEquals(Class.forName(YADAS), fac.getClass());
	}
}
