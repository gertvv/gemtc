package org.drugis.mtc

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Ignore
import org.junit.Before

class InconsistencyStartingValueGeneratorTest
extends ShouldMatchersForJUnit {
	@Test def testInconsistencyEquation() {
		val ta = new Treatment("A")
		val tb = new Treatment("B")
		val tc = new Treatment("C")
		val p1 = new BasicParameter(ta, tb)
		val p2 = new BasicParameter(tb, tc)
		val fp = new BasicParameter(ta, tc)
		val w = new InconsistencyParameter(List(tc, tb, ta, tc))

		val pmtz1 = Map((p1, 1), (p2, 1), (w, -1))
		InconsistencyStartingValueGenerator.inconsistencyEquation(pmtz1, fp) should be (
			Map((p1, 1), (p2, 1), (fp, -1))
		)

		val pmtz2 = Map((p1, 1), (p2, 1), (w, 1))
		InconsistencyStartingValueGenerator.inconsistencyEquation(pmtz2, fp) should be (
			Map((p1, -1), (p2, -1), (fp, 1))
		)
	}

	val EPSILON = 0.0000001
	val network = Network.contFromXML(
		<network type="continuous">
			<treatments>
				<treatment id="Fluvoxamine"></treatment>
				<treatment id="Paroxetine"></treatment>
				<treatment id="Sertraline"></treatment>
			</treatments>
			<studies>
				<study id="Aberg-Wistedt et al, 2000">
					<measurement standardDeviation="1.6" mean="-2.5" sample="177" treatment="Paroxetine" />
					<measurement standardDeviation="1.5" mean="-2.6" sample="176" treatment="Sertraline" />
				</study>
				<study id="Kiev and Feiger, 1997">
					<measurement standardDeviation="1.22" mean="-1.93" sample="30" treatment="Fluvoxamine" />
					<measurement standardDeviation="1.18" mean="-1.52" sample="30" treatment="Paroxetine" />
				</study>
				<study id="Nemeroff et al, 1995">
					<measurement standardDeviation="1.23" mean="-1.35" sample="49" treatment="Fluvoxamine" />
					<measurement standardDeviation="0.96" mean="-1.52" sample="46" treatment="Sertraline" />
				</study>
			</studies>
		</network>
	)

	val fluvo = new Treatment("Fluvoxamine")
	val parox = new Treatment("Paroxetine")
	val sertr = new Treatment("Sertraline")

	val spanningTree = new Tree[Treatment](
		Set((fluvo, parox), (fluvo, sertr)), fluvo)

	val proto = InconsistencyNetworkModel(network, spanningTree)
	val generator = new ContinuousDataStartingValueGenerator(proto)

	@Test def testApply() {
		val basicStart =
			proto.basicParameters.map(p => generator.getRelativeEffect(p.asInstanceOf[BasicParameter]))
		val incons = proto.inconsistencyParameters(0)
		InconsistencyStartingValueGenerator(incons, proto, generator, basicStart) should be ( 0.48 plusOrMinus EPSILON )
	}
}
