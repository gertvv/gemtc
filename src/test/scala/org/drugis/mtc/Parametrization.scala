/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2010 Gert van Valkenhoef.
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

class ParametrizationTest extends ShouldMatchersForJUnit {
	@Test def testCycleClass() {
		val network = Network.noneFromXML(
			<network type="none">
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
						<measurement treatment="B" />
						<measurement treatment="C" />
					</study>
					<study id="3">
						<measurement treatment="B" />
						<measurement treatment="C" />
					</study>
				</studies>
			</network>)

		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")

		val st = new Tree[Treatment](Set[(Treatment, Treatment)](
			(a, b), (a, c)), a)

		val basis = new FundamentalGraphBasis(network.treatmentGraph, st)

		val p = new Parametrization(network, basis)

		val cycle = Cycle(List(a, b, c, a))

		val partition = new Partition(Set(
				new Part(a, b, Set(network.study("1"), network.study("2"))),
				new Part(a, c, Set(network.study("2"))),
				new Part(b, c, Set(network.study("2"), network.study("3")))
			))

		p.cycleClass(cycle) should be (Some((partition, 1)))

		p.inconsistencyClasses should be (Set(partition))
		p.inconsistencyCycles(partition) should be (Set(cycle))
		p.inconsistencyDegree should be (1)
	}

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

	@Test def testConsistencyClass() {
		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")
		val d = new Treatment("D")

		// BCDB has support from only two studies
		val cycle = Cycle(List(b, c, d, b))
		val basis2 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, b), (b, d), (b, c)), a))
		new Parametrization(network, basis2).cycleClass(cycle) should be (None)
	}

	@Test def testNegativeSignClass() {
		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")
		val d = new Treatment("D")

		// ADBCA reduces to ACDA
		val adbca = Cycle(List(a, d, b, c, a)) // -1
		val acda = Cycle(List(a, c, d, a)) // +1
		val basis3 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, c), (a, d), (d, b)), a))
		val param = new Parametrization(network, basis3)

		val partition = new Partition(Set(
				new Part(a, c, Set(network.study("3"))),
				new Part(c, d, Set(network.study("1"))),
				new Part(d, a, Set(network.study("4")))
			))

		param.cycleClass(acda) should be (Some((partition, 1)))
		param.cycleClass(adbca) should be (Some((partition, -1)))
	}

	@Test def testInconsistencyDegree() {
		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")
		val d = new Treatment("D")

		// None of the cycles reduce, all of them have 3 distinct support
		val basis1 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, b), (a, c), (a, d)), a))
		new Parametrization(network, basis1).inconsistencyDegree should be (3)

		// BCDB has support from only two studies
		val basis2 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, b), (b, d), (b, c)), a))
		new Parametrization(network, basis2).inconsistencyDegree should be (2)

		// ACBDA reduces to ACDA
		val basis3 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, c), (a, d), (d, b)), a))
		new Parametrization(network, basis3).inconsistencyDegree should be (2)

		// DBCD has support from only one study
		val basis4 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, d), (d, c), (d, b)), a))
		new Parametrization(network, basis4).inconsistencyDegree should be (2)
	}

	@Test def testBasicParameters() {
		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")
		val d = new Treatment("D")

		val basis2 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, b), (b, d), (b, c)), a))
		new Parametrization(network, basis2).basicParameters should be (
			List[NetworkModelParameter](
				new BasicParameter(a, b),
				new BasicParameter(b, c),
				new BasicParameter(b, d))
		)

		val basis3 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, c), (a, d), (d, b)), a))
		new Parametrization(network, basis3).basicParameters should be (
			List[NetworkModelParameter](
				new BasicParameter(a, c),
				new BasicParameter(a, d),
				new BasicParameter(d, b))
		)
	}

	@Test def testInconsistencyParameters() {
		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")
		val d = new Treatment("D")

		// None of the cycles reduce, all of them have 3 distinct support
		val basis1 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, b), (a, c), (a, d)), a))
		new Parametrization(network, basis1).inconsistencyParameters should be (
			List[NetworkModelParameter](
				new InconsistencyParameter(List(a, b, c, a)),
				new InconsistencyParameter(List(a, b, d, a)),
				new InconsistencyParameter(List(a, c, d, a)))
		)

		// BCDB has support from only two studies
		val basis2 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, b), (b, d), (b, c)), a))
		new Parametrization(network, basis2).inconsistencyParameters should be (
			List[NetworkModelParameter](
				new InconsistencyParameter(List(a, b, c, a)),
				new InconsistencyParameter(List(a, b, d, a)))
		)

		// ACBDA reduces to ACDA
		val basis3 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, c), (a, d), (d, b)), a))
		new Parametrization(network, basis3).inconsistencyParameters should be (
			List[NetworkModelParameter](
				new InconsistencyParameter(List(a, b, d, a)),
				new InconsistencyParameter(List(a, c, d, a)))
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
		val param = new Parametrization(network, basis2)

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
		val param2 = new Parametrization(network, basis2)

		param2(c, d) should be (
			Map((new BasicParameter(b, d), 1),
				(new BasicParameter(b, c), -1)))

		param2(a, d) should be (
			Map((new BasicParameter(b, d), 1),
				(new BasicParameter(a, b), 1),
				(new InconsistencyParameter(List(a, b, d, a)), -1)))

		param2(a, c) should be (
			Map((new BasicParameter(a, b), 1),
				(new BasicParameter(b, c), 1),
				(new InconsistencyParameter(List(a, b, c, a)), -1)))

		// ACBDA reduces to ACDA
		val basis3 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, c), (a, d), (d, b)), a))
		val param3 = new Parametrization(network, basis3)

		param3(a, b) should be (
			Map((new BasicParameter(a, d), 1),
				(new BasicParameter(d, b), 1),
				(new InconsistencyParameter(List(a, b, d, a)), 1)))

		param3(c, d) should be (
			Map((new BasicParameter(a, c), -1),
				(new BasicParameter(a, d), 1),
				(new InconsistencyParameter(List(a, c, d, a)), 1)))

		param3(b, c) should be (
			Map((new BasicParameter(a, d), -1),
				(new BasicParameter(d, b), -1),
				(new InconsistencyParameter(List(a, c, d, a)), -1),
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
		val param3 = new Parametrization(network, basis3)

		param3(a, b) should be (
			Map((new BasicParameter(a, d), 1),
				(new BasicParameter(d, b), 1)))
	}
}
