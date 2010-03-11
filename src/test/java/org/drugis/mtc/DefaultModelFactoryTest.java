package org.drugis.mtc;

import org.junit.Test;
import static org.junit.Assert.*;

public class DefaultModelFactoryTest {
	private static final String YADAS = "org.drugis.mtc.yadas.YadasModelFactory";
	private static final String JAGS = "org.drugis.mtc.jags.JagsModelFactory";

	@Test public void testDefaultInstance() throws Exception {
		ModelFactory fac = DefaultModelFactory.instance();
		assertEquals(Class.forName(YADAS), fac.getClass());
	}

	@Test public void testOtherInstance() throws Exception {
		ModelFactory fac = DefaultModelFactory.instance(JAGS);
		assertEquals(Class.forName(JAGS), fac.getClass());
	}
}
