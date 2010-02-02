package org.drugis.mtc;

import java.util.Map;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class NetworkBuilderTest {
	private Study study(String id, Measurement[] m) {
		return Study$.MODULE$.build(id, m);
	}

	private NetworkBuilder d_builder;
	private Treatment d_ta = new Treatment("A");
	private Treatment d_tb = new Treatment("B");
	private Treatment d_tc = new Treatment("C");
	private Measurement d_s1a = new Measurement(d_ta, 5, 100);
	private Measurement d_s1b = new Measurement(d_tb, 23, 100);
	private Study d_s1 = study("1", new Measurement[]{d_s1a, d_s1b});
	private Measurement d_s2b = new Measurement(d_tb, 12, 43);
	private Measurement d_s2c = new Measurement(d_tc, 15, 40);
	private Study d_s2 = study("2", new Measurement[]{d_s2b, d_s2c});

	@Before public void setUp() {
		d_builder = new NetworkBuilder();
	}

	@Test public void testEmptyBuild() {
		Network n = d_builder.buildNetwork();

		assertNotNull(n);
		assertTrue(n.treatments().isEmpty());
		assertTrue(n.studies().isEmpty());
	}

	@Test public void testBuild() {
		d_builder.add("1", "A", d_s1a.responders(), d_s1a.sampleSize());
		d_builder.add("1", "B", d_s1b.responders(), d_s1b.sampleSize());
		d_builder.add("2", "B", d_s2b.responders(), d_s2b.sampleSize());
		d_builder.add("2", "C", d_s2c.responders(), d_s2c.sampleSize());
		Network n = d_builder.buildNetwork();

		assertNotNull(n);
		assertEquals(3, n.treatments().size());
		assertTrue(n.treatments().contains(d_ta));
		assertTrue(n.treatments().contains(d_tb));
		assertTrue(n.treatments().contains(d_tc));
		assertEquals(2, n.studies().size());
		assertTrue(n.studies().contains(d_s1));
		assertTrue(n.studies().contains(d_s2));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testDuplicateEntry() {
		d_builder.add("1", "A", d_s1a.responders(), d_s1a.sampleSize());
		d_builder.add("1", "A", d_s1b.responders(), d_s1b.sampleSize());
	}
}
