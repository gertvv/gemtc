package org.drugis.mtc

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

class DichotomousDataStartingValueGeneratorTest extends ShouldMatchersForJUnit {
	val network = Network.dichFromXML(
		<network type="dichotomous">
			<treatments>
				<treatment id="Fluoxetine"></treatment>
				<treatment id="Paroxetine"></treatment>
				<treatment id="Sertraline"></treatment>
				<treatment id="Venlafaxine"></treatment>
			</treatments>
			<studies>
				<study id="Alves et al, 1999">
					<measurement sample="47" treatment="Fluoxetine" responders="4"></measurement>
					<measurement sample="40" treatment="Venlafaxine" responders="1"></measurement>
				</study>
				<study id="Ballus et al, 2000">
					<measurement sample="43" treatment="Paroxetine" responders="3"></measurement>
					<measurement sample="41" treatment="Venlafaxine" responders="1"></measurement>
				</study>
				<study id="Bennie et al, 1995">
					<measurement sample="144" treatment="Fluoxetine" responders="8"></measurement>
					<measurement sample="142" treatment="Sertraline" responders="4"></measurement>
				</study>
				<study id="Dierick et al, 1996">
					<measurement sample="161" treatment="Fluoxetine" responders="7"></measurement>
					<measurement sample="153" treatment="Venlafaxine" responders="9"></measurement>
				</study>
				<study id="Fava et al, 2002">
					<measurement sample="92" treatment="Fluoxetine" responders="11"></measurement>
					<measurement sample="96" treatment="Paroxetine" responders="8"></measurement>
					<measurement sample="96" treatment="Sertraline" responders="6"></measurement>
				</study>
				<study id="Newhouse et al, 2000">
					<measurement sample="119" treatment="Fluoxetine" responders="15"></measurement>
					<measurement sample="117" treatment="Sertraline" responders="17"></measurement>
				</study>
				<study id="Sechter et al, 1999">
					<measurement sample="120" treatment="Fluoxetine" responders="2"></measurement>
					<measurement sample="118" treatment="Sertraline" responders="4"></measurement>
				</study>
			</studies>
		</network>
	)

	val fluox = new Treatment("Fluoxetine")
	val parox = new Treatment("Paroxetine")
	val venla = new Treatment("Venlafaxine")
	val sertr = new Treatment("Sertraline")

	val spanningTree = new Tree[Treatment](
		Set((fluox, parox), (fluox, venla), (fluox, sertr)), fluox)

	val proto = InconsistencyNetworkModel(network, spanningTree)
	val generator = new DichotomousDataStartingValueGenerator(proto)

	@Test @Ignore def testGenerateBaselineEffect() {
		val m0 = network.study("Alves et al, 1999").measurements(fluox)
		generator.getBaselineEffect(network.study("Alves et al, 1999")) should be (
			Math.log(
				(m0.responders + 0.5) / (m0.sampleSize - m0.responders + 0.5))
		)
	}
}
