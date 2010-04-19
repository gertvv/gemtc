package org.drugis.mtc.yadas

import org.drugis.mtc._
import gov.lanl.yadas._

import org.apache.commons.math.stat.descriptive.moment.StandardDeviation
import org.apache.commons.math.stat.descriptive.moment.Mean
import org.apache.commons.math.linear.ArrayRealVector

class YadasInconsistencyModel[M <: Measurement](network: Network[M])
extends YadasModel(network, true) with InconsistencyModel {
	def this(model: NetworkModel[M]) {
		this(model.network)
		proto = model
	}
}
