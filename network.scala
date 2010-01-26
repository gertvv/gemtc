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

val studyList = network.studies.toList.sort((a, b) => a.id < b.id)
val studyMap = indexMap(studyList)
val treatmentList = network.treatments.toList.sort((a, b) => a < b)
val treatmentMap = indexMap(treatmentList)

val data = studyList.flatMap(
	study => study.measurements.map(m => (study, m._2)))

val s = data.map(a => studyMap(a._1))

val b = assignBaselines(network, best)

println("s <- c(" + s.mkString(", ") + ")")
println("t <- c(" + data.map(a => treatmentMap(a._2.treatment)).mkString(", ") + ")")
println("r <- c(" + data.map(a => a._2.responders).mkString(", ") + ")")
println("n <- c(" + data.map(a => a._2.sampleSize).mkString(", ") + ")")
println("b <- c(" + studyList.map(s => treatmentMap(b(s))).mkString(", ") + ")")

def expressEffect(network: Network, baseline: Treatment, effect: Treatment) = {
	"EXPR"
}

println()
println("model {")
println("\t# Study baseline effects")
println("\tfor (i 1:length(b)) {")
println("\t\tmu[i] ~ dnorm(0, .001)")
println("\t}")
println()
for (study <- studyList; treatment <- study.treatments) {
	if (study.treatments.size > 2) {
		println("\t# Skipping multi-arm study " + studyMap(study))
	} else {
		val i = studyMap(study)
		val j = treatmentMap(treatment)
		val bi = treatmentMap(b(study))
		print("\tdelta[" + i + ", " + bi + ", " + j + "] ")
		if (j == bi) println("<- 0")
		else println("~ dnorm(" + expressEffect(network, b(study), treatment) + ", tau)")
	}
}
println()
println("\t# For each measurement, give p")
println("\tfor (i in 1:length(s)) { ")
println("\t\tlogit(p[s[i], t[i]]) <- mu[s[i]] + delta[s[i], b[s[i]], t[i]]")
println("\t\tr[i] ~ dbin(p[s[i], t[i]], n[i])")
println("\t}")
println()
println("\t# Definition of w's, d's and tau's should follow")
println("}")
