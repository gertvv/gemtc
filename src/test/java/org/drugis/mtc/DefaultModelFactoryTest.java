package org.drugis.mtc;

import org.junit.Test;
import static org.junit.Assert.*;

public class DefaultModelFactoryTest {
	private static final String JAGS = "org.drugis.mtc.jags.JagsModelFactory";
	private static final String GIBBS = "org.drugis.mtc.gibbs.GibbsModelFactory";

	@Test public void testDefaultInstance() throws Exception {
		ModelFactory fac = DefaultModelFactory.instance();
		assertEquals(Class.forName(JAGS), fac.getClass());
	}

	@Test public void testOtherInstance() throws Exception {
		ModelFactory fac = DefaultModelFactory.instance(GIBBS);
		assertEquals(Class.forName(GIBBS), fac.getClass());
	}
}
