package org.drugis.mtc

class DichotomousDataStartingValueGenerator[
		P <: Parametrization[DichotomousMeasurement]](
val model: NetworkModel[DichotomousMeasurement, P])
extends StartingValueGenerator[DichotomousMeasurement] {
	override def getBaselineEffect(study: Study[DichotomousMeasurement]) = 0.0 // TODO
	override def getRandomEffect(study: Study[DichotomousMeasurement], p: BasicParameter) = 0.0 // TODO
	override def getRelativeEffect(p: BasicParameter) = 0.0 // TODO
	override def getRandomEffectsVariance() = 0.25 // TODO
}
