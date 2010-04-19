package org.drugis.mtc.yadas

import org.drugis.mtc._
import gov.lanl.yadas._

import org.apache.commons.math.stat.descriptive.moment.StandardDeviation
import org.apache.commons.math.stat.descriptive.moment.Mean
import org.apache.commons.math.linear.ArrayRealVector

class YadasInconsistencyModel(proto: NetworkModel[_ <: Measurement])
extends YadasModel(proto, true) with InconsistencyModel {
}
