package org.drugis.mtc.parameterization;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.junit.Before;
import org.junit.Test;

import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.UndirectedGraph;

public class ConsistencyParameterizationTest {
	private Network d_network;
	private Study d_s1;
	private Study d_s2;
	private Study d_s3;
	private Study d_s4;
	
	@Before
	public void setUp() {
		d_network = new Network();
		Treatment ta = new Treatment("A");
		Treatment tb = new Treatment("B");
		Treatment tc = new Treatment("C");
		Treatment td = new Treatment("D");
		d_network.getTreatments().addAll(Arrays.asList(ta, tb, tc, td));
		d_s1 = new Study("1");
		d_s1.getMeasurements().addAll(Arrays.asList(new Measurement(td), new Measurement(tb), new Measurement(tc)));
		d_s2 = new Study("2");
		d_s2.getMeasurements().addAll(Arrays.asList(new Measurement(ta), new Measurement(tb)));
		d_s3 = new Study("3");
		d_s3.getMeasurements().addAll(Arrays.asList(new Measurement(ta), new Measurement(tc)));
		d_s4 = new Study("4");
		d_s4.getMeasurements().addAll(Arrays.asList(new Measurement(ta), new Measurement(td)));
		d_network.getStudies().addAll(Arrays.asList(d_s1, d_s2, d_s3, d_s4));
	}
	
	@Test
	public void testMinimumDiamaterTree() {
		UndirectedGraph<Treatment, Collection<Study>> cGraph = NetworkModel.createComparisonGraph(d_network);
		for (Collection<Study> e : cGraph.getEdges()) {
			System.out.println(e + " " + e.hashCode() + " " + cGraph.getEndpoints(e));
			
		}
		System.out.println(cGraph.getEndpoints(new ArrayList<Study>(Arrays.asList(d_s2))));
		Tree<Treatment, Collection<Study>> tree = ConsistencyParameterization.findSpanningTree(cGraph);
		assertEquals(new Treatment("A"), tree.getRoot());
	}
	
/*
class ConsistencyParametrizationTest extends ShouldMatchersForJUnit {
	val network = Network.noneFromXML(<network type="none">
		<treatments>
			<treatment id="A"/>
			<treatment id="B"/>
			<treatment id="C"/>
			<treatment id="D"/>
		</treatments>
		<studies>
			<study id="1">
				<measurement treatment="D" />
				<measurement treatment="B" />
				<measurement treatment="C" />
			</study>
			<study id="2">
				<measurement treatment="A" />
				<measurement treatment="B" />
			</study>
			<study id="3">
				<measurement treatment="A" />
				<measurement treatment="C" />
			</study>
			<study id="4">
				<measurement treatment="A" />
				<measurement treatment="D" />
			</study>
		</studies>
	</network>)

	@Test def testBasicParameters() {
		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")
		val d = new Treatment("D")

		val basis2 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, b), (b, d), (b, c)), a))
		new ConsistencyParametrization(network, basis2).basicParameters should be (
			List[NetworkModelParameter](
				new BasicParameter(a, b),
				new BasicParameter(b, c),
				new BasicParameter(b, d))
		)

		val basis3 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, c), (a, d), (d, b)), a))
		new ConsistencyParametrization(network, basis3).basicParameters should be (
			List[NetworkModelParameter](
				new BasicParameter(a, c),
				new BasicParameter(a, d),
				new BasicParameter(d, b))
		)
	}

	@Test def testParameterizationBasic() {
		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")
		val d = new Treatment("D")

		val basis2 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, b), (b, d), (b, c)), a))
		val param = new ConsistencyParametrization(network, basis2)

		param(a, b) should be (
			Map((new BasicParameter(a, b), 1)))
		param(b, a) should be (
			Map((new BasicParameter(a, b), -1)))
	}

	@Test def testParameterizationFunctional() {
		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")
		val d = new Treatment("D")

		val basis2 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, b), (b, d), (b, c)), a))
		val param2 = new ConsistencyParametrization(network, basis2)

		param2(c, d) should be (
			Map((new BasicParameter(b, d), 1),
				(new BasicParameter(b, c), -1)))

		param2(a, d) should be (
			Map((new BasicParameter(b, d), 1),
				(new BasicParameter(a, b), 1)))

		param2(a, c) should be (
			Map((new BasicParameter(a, b), 1),
				(new BasicParameter(b, c), 1)))

		// ACBDA reduces to ACDA
		val basis3 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, c), (a, d), (d, b)), a))
		val param3 = new ConsistencyParametrization(network, basis3)

		param3(a, b) should be (
			Map((new BasicParameter(a, d), 1),
				(new BasicParameter(d, b), 1)))

		param3(c, d) should be (
			Map((new BasicParameter(a, c), -1),
				(new BasicParameter(a, d), 1)))

		param3(b, c) should be (
			Map((new BasicParameter(a, d), -1),
				(new BasicParameter(d, b), -1), 
				(new BasicParameter(a, c), 1)))
	}

	@Test def testParamForNonExistantCycle() {
		val network = Network.noneFromXML(<network type="none">
			<treatments>
				<treatment id="A"/>
				<treatment id="B"/>
				<treatment id="C"/>
				<treatment id="D"/>
			</treatments>
			<studies>
				<study id="1">
					<measurement treatment="D" />
					<measurement treatment="B" />
					<measurement treatment="C" />
				</study>
				<study id="3">
					<measurement treatment="A" />
					<measurement treatment="C" />
				</study>
				<study id="4">
					<measurement treatment="A" />
					<measurement treatment="D" />
				</study>
			</studies>
		</network>)

		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")
		val d = new Treatment("D")

		val basis3 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, c), (a, d), (d, b)), a))
		val param3 = new ConsistencyParametrization(network, basis3)

		param3(a, b) should be (
			Map((new BasicParameter(a, d), 1),
				(new BasicParameter(d, b), 1)))
	}
}
 */
}
