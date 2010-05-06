package org.drugis.mtc.yadas

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

import org.drugis.mtc._

class ContinuousDataIT extends ShouldMatchersForJUnit {
	// data from Welton et. al., Am J Epidemiol 2009;169:1158–1165
	val m = -1.362791 // mean(d)
	val s = 0.982033 // sd(d)
	val f = 0.05

	def network = {
		val is = classOf[ContinuousDataIT].getResourceAsStream("weltonBP.xml")
		Network.contFromXML(scala.xml.XML.load(is))
	}

	val psych = new Treatment("psych")
	val usual = new Treatment("usual")

	@Test def testResult() {
		val model = new YadasConsistencyModel(network)
		model.run()
		
		model.isReady should be (true)
		val d = model.getRelativeEffect(usual, psych)
		d.getMean should be (m plusOrMinus f * s)
		d.getStandardDeviation should be (s plusOrMinus f * s)
	}
}
