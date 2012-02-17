package org.drugis.mtc.parameterization;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.drugis.mtc.data.DataType;
import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.junit.Before;
import org.junit.Test;

public class PriorGeneratorTest {
	private static final double EPSILON = 0.00000001;
	private Network d_networkDich;
	private Network d_networkCont;
	
	@Before
	public void setUp() {
		Treatment ta = new Treatment("A");
		Treatment tb = new Treatment("B");
		Treatment tc = new Treatment("C");
		
		Study s1 = new Study("1");
		s1.getMeasurements().addAll(Arrays.asList(
				new Measurement(ta, 12, 100),
				new Measurement(tb, 14, 100)));
		Study s2 = new Study("2");
		s2.getMeasurements().addAll(Arrays.asList(
				new Measurement(ta, 30, 100),
				new Measurement(tb, 35, 100)));
		Study s3 = new Study("3");
		s3.getMeasurements().addAll(Arrays.asList(
				new Measurement(ta, 20, 100),
				new Measurement(tb, 28, 100),
				new Measurement(tc, 30, 100)));
		d_networkDich = new Network();
		d_networkDich.setType(DataType.RATE);
		d_networkDich.getTreatments().addAll(Arrays.asList(ta, tb, tc));
		d_networkDich.getStudies().addAll(Arrays.asList(s1, s2, s3));
		
		Study s4 = new Study("4");
		s4.getMeasurements().addAll(Arrays.asList(
				new Measurement(ta, 12.0, 8.0, 100),
				new Measurement(tb, 18.0, 8.0, 100)));
		Study s5 = new Study("5");
		s5.getMeasurements().addAll(Arrays.asList(
				new Measurement(ta, 10.0, 8.0, 100),
				new Measurement(tb, 20.0, 8.0, 100)));
		Study s6 = new Study("6");
		s6.getMeasurements().addAll(Arrays.asList(
				new Measurement(ta, 28.0, 8.0, 100),
				new Measurement(tb, 25.0, 8.0, 100)));
		d_networkCont = new Network();
		d_networkCont.setType(DataType.CONTINUOUS);
		d_networkCont.getTreatments().addAll(Arrays.asList(ta, tb, tc));
		d_networkCont.getStudies().addAll(Arrays.asList(s4, s5, s6));
	}

	@Test
	public void testPriors() {
		assertEquals(0.43414982369413, new PriorGenerator(d_networkDich).getRandomEffectsSigma(), EPSILON);
		assertEquals(0.43414982369413, new PriorGenerator(d_networkDich).getInconsistencySigma(), EPSILON);

		assertEquals(7.0, new PriorGenerator(d_networkCont).getRandomEffectsSigma(), EPSILON);
		assertEquals(7.0, new PriorGenerator(d_networkCont).getInconsistencySigma(), EPSILON);
		
		assertEquals(Math.sqrt(42.4093656180699), new PriorGenerator(d_networkDich).getVagueNormalSigma(), EPSILON);
		assertEquals(Math.sqrt(11025.00), new PriorGenerator(d_networkCont).getVagueNormalSigma(), EPSILON);
	}
}
