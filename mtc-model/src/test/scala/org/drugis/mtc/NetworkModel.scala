/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
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

package org.drugis.mtc

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

class ConsistencyNetworkModelTest extends ShouldMatchersForJUnit {
	val network = Network.noneFromXML(<network>
		<treatments>
			<treatment id="A"/>
			<treatment id="B"/>
			<treatment id="C"/>
			<treatment id="D"/>
			<treatment id="E"/>
			<treatment id="F"/>
		</treatments>
		<studies>
			<study id="1">
				<measurement treatment="C"/>
				<measurement treatment="F"/>
			</study>
			<study id="2">
				<measurement treatment="B"/>
				<measurement treatment="C"/>
				<measurement treatment="D"/>
			</study>
			<study id="3">
				<measurement treatment="A"/>
				<measurement treatment="E"/>
				<measurement treatment="F"/>
			</study>
		</studies>
	</network>)

	val ta = new Treatment("A")
	val tb = new Treatment("B")
	val tc = new Treatment("C")
	val td = new Treatment("D")
	val te = new Treatment("E")
	val tf = new Treatment("F")
	val studies = network.studies.toList.sort((a, b) => a.id < b.id)

	@Test def testModelCreation() {
		val model = ConsistencyNetworkModel(network)
		val expectedTree = new Tree[Treatment](
			Set((tc, tf), (tc, tb), (tc, td), (tf, te), (tf, ta)), tc)
		model.basis.tree should be (expectedTree)
		model.studyBaseline(studies(0)) should be (tc)
		model.studyBaseline(studies(1)) should be (tc)
		model.studyBaseline(studies(2)) should be (tf)
	}
}

class NetworkModelTest extends ShouldMatchersForJUnit {
	val network = Network.dichFromXML(<network>
			<treatments>
				<treatment id="A"/>
				<treatment id="B"/>
				<treatment id="C"/>
				<treatment id="D"/>
			</treatments>
			<studies>
				<study id="1">
					<measurement treatment="A" responders="1" sample="100" />
					<measurement treatment="B" responders="1" sample="100" />
					<measurement treatment="C" responders="1" sample="100" />
				</study>
				<study id="2">
					<measurement treatment="B" responders="1" sample="100" />
					<measurement treatment="C" responders="1" sample="100" />
					<measurement treatment="D" responders="1" sample="100" />
				</study>
				<study id="3">
					<measurement treatment="A" responders="1" sample="100" />
					<measurement treatment="C" responders="1" sample="100" />
				</study>
			</studies>
		</network>)

	val ta = new Treatment("A")
	val tb = new Treatment("B")
	val tc = new Treatment("C")
	val td = new Treatment("D")
	val studies = network.studies.toList.sort((a, b) => a.id < b.id)

	val spanningTree = new Tree[Treatment](
		Set((ta, tc), (ta, tb), (tb, td)), ta)

	@Test def testAssignBaselines() {
		val pmtz = new InconsistencyParametrization(network,
			new FundamentalGraphBasis(network.treatmentGraph, spanningTree))
		val baselines = InconsistencyNetworkModel.assignBaselines(pmtz)
		baselines(studies(0)) should be (tb)
		baselines(studies(1)) should be (td)
		baselines(studies(2)) should be (ta)
	}

	@Test def testStudyList() {
		NetworkModel.studyList(network.studies) should be (studies)
	}

	@Test def testTreatmentList() {
		NetworkModel.treatmentList(network.treatments) should be (
			List(ta, tb, tc, td))
	}

	@Test def testIndexMap() {
		val map = NetworkModel.indexMap(studies)
		for (i <- 0 until studies.length) {
			map(studies(i)) should be (i + 1)
		}
	}

	@Test def testFactory() {
		val model = InconsistencyNetworkModel(network, spanningTree)
		model.studyBaseline(studies(0)) should be (tb)
		model.studyBaseline(studies(1)) should be (td)
		model.studyBaseline(studies(2)) should be (ta)
		model.treatmentList should be (List(ta, tb, tc, td))
		model.studyList should be (studies)
		model.basis.tree should be (spanningTree)
		model.basis.graph should be (network.treatmentGraph)
	}

	@Test def testData() {
		val data = InconsistencyNetworkModel(network, spanningTree).data
		data.size should be (8)
		data(0)._1 should be (studies(0))
		data(1)._1 should be (studies(0))
		data(2)._1 should be (studies(0))
		data(3)._1 should be (studies(1))
		data(4)._1 should be (studies(1))
		data(5)._1 should be (studies(1))
		data(6)._1 should be (studies(2))
		data(7)._1 should be (studies(2))
		data(0)._2.treatment should be (ta)
		data(1)._2.treatment should be (tb)
		data(2)._2.treatment should be (tc)
		data(3)._2.treatment should be (tb)
		data(4)._2.treatment should be (tc)
		data(5)._2.treatment should be (td)
		data(6)._2.treatment should be (ta)
		data(7)._2.treatment should be (tc)
	}

	@Test def testParameterVector() {
		val model = InconsistencyNetworkModel(network, spanningTree)
		model.parameterVector should be (List[NetworkModelParameter](
				new BasicParameter(ta, tb),
				new BasicParameter(ta, tc),
				new BasicParameter(tb, td),
				new InconsistencyParameter(List(ta, tb, tc, ta))
			))
	}

	@Test def testParameterVectorOnlyInconsistencies() {
		val model = InconsistencyNetworkModel(network, new Tree[Treatment](
			Set((ta, tc), (tc, tb), (tb, td)), ta))
		model.parameterVector should be (List[NetworkModelParameter](
				new BasicParameter(ta, tc),
				new BasicParameter(tb, td),
				new BasicParameter(tc, tb),
				new InconsistencyParameter(List(ta, tb, tc, ta))
			))
	}

	@Test def testRelativeEffects() {
		val model = InconsistencyNetworkModel(network, spanningTree)
		// assignment of baselines is 1:B, 2:D, 3:A
		model.relativeEffects should be (
			List((tb, ta), (tb, tc), (td, tb), (td, tc), (ta, tc)))
	}

	@Test def testRelativeEffectIndex() {
		val model = InconsistencyNetworkModel(network, spanningTree)
		model.relativeEffectIndex(studies(0)) should be (0)
		model.relativeEffectIndex(studies(1)) should be (2)
		model.relativeEffectIndex(studies(2)) should be (4)
	}

	val networkDich = Network.dichFromXML(<network>
			<treatments>
				<treatment id="A"/>
				<treatment id="B"/>
				<treatment id="C"/>
			</treatments>
			<studies>
				<study id="1">
					<measurement treatment="A" responders="12" sample="100" />
					<measurement treatment="B" responders="14" sample="100" />
				</study>
				<study id="2">
					<measurement treatment="A" responders="30" sample="100" />
					<measurement treatment="B" responders="35" sample="100" />
				</study>
				<study id="3">
					<measurement treatment="A" responders="20" sample="100" />
					<measurement treatment="B" responders="28" sample="100" />
					<measurement treatment="C" responders="30" sample="100" />
				</study>
			</studies>
		</network>)

	val networkCont = Network.contFromXML(<network type="continuous">
			<treatments>
				<treatment id="A"/>
				<treatment id="B"/>
			</treatments>
			<studies>
				<study id="1">
					<measurement treatment="A" mean="12" standardDeviation="8" sample="100" />
					<measurement treatment="B" mean="18" standardDeviation="8" sample="100" />
				</study>
				<study id="2">
					<measurement treatment="A" mean="10" standardDeviation="8" sample="100" />
					<measurement treatment="B" mean="20" standardDeviation="8" sample="100" />
				</study>
				<study id="3">
					<measurement treatment="A" mean="28" standardDeviation="8" sample="100" />
					<measurement treatment="B" mean="25" standardDeviation="8" sample="100" />
				</study>
			</studies>
		</network>)

	val networkNone = Network.noneFromXML(<network type="none">
			<treatments>
				<treatment id="A"/>
				<treatment id="B"/>
			</treatments>
			<studies>
				<study id="1">
					<measurement treatment="A" />
					<measurement treatment="B" />
				</study>
				<study id="2">
					<measurement treatment="A" />
					<measurement treatment="B" />
				</study>
				<study id="3">
					<measurement treatment="A" />
					<measurement treatment="B" />
				</study>
			</studies>
		</network>)

	@Test def testVariancePrior() {
		InconsistencyNetworkModel(networkDich).variancePrior should be (
			0.43414982369413 plusOrMinus 0.000001)
		InconsistencyNetworkModel(networkCont).variancePrior should be (
			7.0 plusOrMinus 0.000001)
	}

	@Test def testNormalPrior() {
		InconsistencyNetworkModel(networkDich).normalPrior should be (
			42.4093656180699 plusOrMinus 0.00000001)
		InconsistencyNetworkModel(networkCont).normalPrior should be (
			11025.00 plusOrMinus 0.000001)

	}

	@Test def testNoneModel() {
		val model = InconsistencyNetworkModel(networkNone)
		model.treatmentList should be (List(new Treatment("A"), new Treatment("B")))
		model.studyList.size should be (3)
	}
}

class NodeSplitNetworkModelTest extends ShouldMatchersForJUnit {
	val network = Network.noneFromXML(<network type="none">
			<treatments>
				<treatment id="A"/>
				<treatment id="B"/>
				<treatment id="C"/>
				<treatment id="D"/>
			</treatments>
			<studies>
				<study id="1">
					<measurement treatment="A" />
					<measurement treatment="B" />
					<measurement treatment="D" />
				</study>
				<study id="2">
					<measurement treatment="A" />
					<measurement treatment="C" />
					<measurement treatment="D" />
				</study>
				<study id="3">
					<measurement treatment="B" />
					<measurement treatment="C" />
					<measurement treatment="D" />
				</study>
			</studies>
		</network>)
	val ta = new Treatment("A")
	val tb = new Treatment("B")
	val tc = new Treatment("C")
	val td = new Treatment("D")
	val studies = network.studies.toList.sort((a, b) => a.id < b.id)

	val spanningTree = new Tree[Treatment](
		Set((td, tc), (td, tb), (td, ta)), td)

	@Test def testAssignBaselines() {
		val pmtz = new NodeSplitParametrization(network,
			new FundamentalGraphBasis(network.treatmentGraph, spanningTree),
			(td, tc))
		val baselines = NodeSplitNetworkModel.assignBaselines(pmtz)
		baselines(studies(0)) should be (ta)
		baselines(studies(1)) should be (td)
		baselines(studies(2)) should be (td)
	}

	@Test def testModelCreation() {
		val model = NodeSplitNetworkModel(network, (td, tc))
		val expectedTree = new Tree[Treatment](
			Set((td, tc), (tc, tb), (tb, ta)), td)
		model.basis.tree should be (expectedTree)
		model.studyBaseline(studies(0)) should be (ta)
		model.studyBaseline(studies(1)) should be (td)
		model.studyBaseline(studies(2)) should be (tc)
	}

	@Test def testSplittableNodes() {
		val network = Network.noneFromXML(<network type="none">
				<treatments>
					<treatment id="A"/>
					<treatment id="B"/>
					<treatment id="C"/>
					<treatment id="D"/>
					<treatment id="E"/>
				</treatments>
				<studies>
					<study id="1">
						<measurement treatment="A" />
						<measurement treatment="B" />
					</study>
					<study id="2">
						<measurement treatment="A" />
						<measurement treatment="C" />
					</study>
					<study id="3">
						<measurement treatment="B" />
						<measurement treatment="C" />
					</study>
					<study id="4">
						<measurement treatment="C" />
						<measurement treatment="D" />
					</study>
					<study id="5">
						<measurement treatment="C" />
						<measurement treatment="E" />
					</study>
				</studies>
			</network>)

		NodeSplitNetworkModel.getSplittableNodes(network) should be (
			Set((ta, tb), (ta, tc), (tb, tc)))
	}

	@Test def testSplittableNodesNone() {
		val network = Network.noneFromXML(<network type="none">
				<treatments>
					<treatment id="A"/>
					<treatment id="B"/>
					<treatment id="C"/>
				</treatments>
				<studies>
					<study id="1">
						<measurement treatment="A" />
						<measurement treatment="B" />
					</study>
					<study id="2">
						<measurement treatment="A" />
						<measurement treatment="C" />
					</study>
				</studies>
			</network>)

		NodeSplitNetworkModel.getSplittableNodes(network) should be (Set())
	}

	@Test def testSplittableNodesOneCycle() {
		val network = Network.noneFromXML(<network type="none">
				<treatments>
					<treatment id="A"/>
					<treatment id="B"/>
					<treatment id="C"/>
				</treatments>
				<studies>
					<study id="1">
						<measurement treatment="A" />
						<measurement treatment="B" />
					</study>
					<study id="2">
						<measurement treatment="A" />
						<measurement treatment="C" />
					</study>
					<study id="3">
						<measurement treatment="B" />
						<measurement treatment="C" />
					</study>
				</studies>
			</network>)

		NodeSplitNetworkModel.getSplittableNodes(network)
	}
}

class AdditionalBaselineAssignmentTest extends ShouldMatchersForJUnit {
	val network = Network.dichFromXML(<network>
			<treatments>
				<treatment id="A"/>
				<treatment id="B"/>
				<treatment id="C"/>
				<treatment id="D"/>
			</treatments>
			<studies>
				<study id="1">
					<measurement treatment="A" responders="1" sample="100" />
					<measurement treatment="B" responders="1" sample="100" />
					<measurement treatment="C" responders="1" sample="100" />
				</study>
				<study id="2">
					<measurement treatment="B" responders="1" sample="100" />
					<measurement treatment="C" responders="1" sample="100" />
					<measurement treatment="D" responders="1" sample="100" />
				</study>
			</studies>
		</network>)

	val ta = new Treatment("A")
	val tb = new Treatment("B")
	val tc = new Treatment("C")
	val td = new Treatment("D")

	val spanningTree = new Tree[Treatment](
		Set((ta, tc), (ta, tb), (tb, td)), ta)

	// Too strict problem defn. would chocke on this
	@Test def testAssignBaselines() {
		val pmtz = new InconsistencyParametrization(network,
			new FundamentalGraphBasis(network.treatmentGraph, spanningTree))
		val baselines = InconsistencyNetworkModel.assignBaselines(pmtz)
		baselines should not be (null)
	}
}

class BasicParameterTest extends ShouldMatchersForJUnit {
	@Test def testToString() {
		new BasicParameter(new Treatment("A"), new Treatment("B")
			).toString should be ("d.A.B")
	}

	@Test def testEquals() {
		new BasicParameter(new Treatment("A"), new Treatment("B")) should be (
			new BasicParameter(new Treatment("A"), new Treatment("B")))
	}
}

class InconsistencyParameterTest extends ShouldMatchersForJUnit {
	@Test def testToString() {
		new InconsistencyParameter(
				List(new Treatment("A"), new Treatment("B"),
				new Treatment("C"), new Treatment("A"))
			).toString should be ("w.A.B.C")
	}

	@Test def testEquals() {
		new InconsistencyParameter(
				List(new Treatment("A"), new Treatment("B"),
				new Treatment("C"), new Treatment("A"))) should be (
			new InconsistencyParameter(
				List(new Treatment("A"), new Treatment("B"),
				new Treatment("C"), new Treatment("A")))
			)
	}

	@Test def testConstructFromJavaList() {
		val list = new java.util.ArrayList[Treatment]()
		list.add(new Treatment("A"))
		list.add(new Treatment("B"))
		list.add(new Treatment("C"))
		list.add(new Treatment("A"))

		new InconsistencyParameter(list) should be (
			new InconsistencyParameter(
				List(new Treatment("A"), new Treatment("B"),
				new Treatment("C"), new Treatment("A")))
			)
	}
}
