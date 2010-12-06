package org.drugis.mtc

class PriorStartingValueGenerator[M <: Measurement, P <: Parametrization[M]](
model: NetworkModel[M, P])
extends StartingValueGenerator[M] {
	override def getBaselineEffect(study: Study[M]) = 0.0
	override def getRandomEffect(study: Study[M], p: BasicParameter) = 0.0
	override def getRelativeEffect(p: BasicParameter) = 0.0
	override def getRandomEffectsVariance() = 0.25
}
