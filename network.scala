import org.drugis._

if (args.size != 1) {
	println("Usage: network.scala <data-xml-file>")
	exit()
}

val xml = scala.xml.XML.loadFile(args(0))

val network = Network.fromXML(xml)

val top = new Treatment("A")

for (st <- network.treeEnumerator(top)) {
	println(st + " has ICDF = " + network.countInconsistencies(st))
}

val best = network.bestSpanningTree(top)
println("BEST: " + best + " has ICDF = " + network.countInconsistencies(best))


println()

val model = NetworkModel(network, best)

println("s <- c(" + model.data.map(a => model.studyMap(a._1)).mkString(", ") + ")")
println("t <- c(" + model.data.map(a => model.treatmentMap(a._2.treatment)).mkString(", ") + ")")
println("r <- c(" + model.data.map(a => a._2.responders).mkString(", ") + ")")
println("n <- c(" + model.data.map(a => a._2.sampleSize).mkString(", ") + ")")
println("b <- c(" + model.studyList.map(s => model.treatmentMap(model.studyBaseline(s))).mkString(", ") + ")")

def expressParam(p: NetworkModelParameter, v: Int): String = 
	v match {
		case  1 => p.toString
		case -1 => "-" + p.toString
		case  _ => throw new Exception("Unexpected value!")
	}

def expressParams(params: Map[NetworkModelParameter, Int]): String = {
	(for {(p, v) <- params.filter(a => a._2 != 0)} yield expressParam(p, v)).mkString(" + ")
}

def expressEffect(model: NetworkModel, study: Study, effect: Treatment) = {
	val base = model.studyBaseline(study)
	require(effect != base)
	expressParams(model.parameterization(base, effect))
}

def twoArmDelta(model: NetworkModel, study: Study) {
	println("\t# Random effects in study " + study.id)
	val treatment = (study.treatments - model.studyBaseline(study)).toList.first
	val i = model.studyMap(study)
	val j = model.treatmentMap(treatment)
	val bi = model.treatmentMap(model.studyBaseline(study))
	println("\tdelta[" + i + ", " + bi + ", " + bi + "] <- 0")
	println("\tdelta[" + i + ", " + bi + ", " + j + "] ~ dnorm(" +
		expressEffect(model, study, treatment) + ", tau.d)")
}

def multiArmDelta(model: NetworkModel, study: Study) {
	println("\t# Random effects in study " + study.id)
	val i = model.studyMap(study)
	val bi = model.treatmentMap(model.studyBaseline(study))
	val treatments = (study.treatments - model.studyBaseline(study)).toList
	val n = treatments.size

	for (k <- 1 to n) {
		println("\td[" + i + ", " + k + "] <- " +
			expressEffect(model, study, treatments(k - 1)))
	}

	println("\tre[" + i + ", 1:" + n + "] ~ " +
		"dmnorm(d[" + i + ", 1:" + n + "], tau." + n + ")")

	println("\tdelta[" + i + ", " + bi + ", " + bi + "] <- 0")
	for (k <- 1 to n) {
		val j = model.treatmentMap(treatments(k - 1))
		println("\tdelta[" + i + ", " + bi + ", " + j + "] <- " +
			"re[" + i + ", " + k + "]")
	}
}

def tauMatrix(dim: Int) {
	if (dim == 2) {
		println("\t# 2x2 inv. covariance matrix for 3-arm trials")
		println("\ttau.2[1, 1] <- tau.d")
		println("\ttau.2[1, 2] <- -tau.d / 2")
		println("\ttau.2[2, 1] <- -tau.d / 2")
		println("\ttau.2[2, 2] <- tau.d")
	} else {
		throw new Exception("Studies with > 3 arms not supported yet")
	}
}

println()
println("model {")
println("\t# Study baseline effects")
println("\tfor (i in 1:length(b)) {")
println("\t\tmu[i] ~ dnorm(0, .001)")
println("\t}")
println()
for (study <- model.studyList) {
	if (study.treatments.size == 2) {
		twoArmDelta(model, study)
	} else {
		multiArmDelta(model, study)
	}
}
println()
println("\t# For each measurement, give p")
println("\tfor (i in 1:length(s)) { ")
println("\t\tlogit(p[s[i], t[i]]) <- mu[s[i]] + delta[s[i], b[s[i]], t[i]]")
println("\t\tr[i] ~ dbin(p[s[i], t[i]], n[i])")
println("\t}")
println()
for (param <- model.parameterVector) {
	val tau = param match {
		case basic: BasicParameter => ".001"
		case incon: InconsistencyParameter => "tau.w"
	}
	println("\t" + param.toString + " ~ dnorm(0, " + tau + ")") 
}
println()
println("\tsd.w ~ dunif(0.00001, 2)")
println("\tvar.w <- sd.w * sd.w")
println("\ttau.w <- 1/ var.w")
println()
println("\tsd.d ~ dunif(0.00001, 2)")
println("\tvar.d <- sd.d * sd.d")
println("\ttau.d <- 1/ var.d")
println()
for (n <- model.network.studies.map(a => a.treatments.size)) {
	if (n > 2) tauMatrix(n - 1)
}
println("}")
