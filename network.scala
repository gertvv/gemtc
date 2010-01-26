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

def indexMap[T](l: List[T]): Map[T, Int] =
	Map[T, Int]() ++ l.map(a => (a, l.indexOf(a) + 1))

val studyList = network.studies.toList.sort((a, b) => a.id < b.id)
val studyMap = indexMap(studyList)
val treatmentList = network.treatments.toList.sort((a, b) => a < b)
val treatmentMap = indexMap(treatmentList)

val data = studyList.flatMap(
	study => study.measurements.map(m => (study, m._2)))

val s = data.map(a => studyMap(a._1))

println("s <- c(" + s.mkString(", ") + ")")
println("t <- c(" + data.map(a => treatmentMap(a._2.treatment)).mkString(", ") + ")")
println("r <- c(" + data.map(a => a._2.responders).mkString(", ") + ")")
println("n <- c(" + data.map(a => a._2.sampleSize).mkString(", ") + ")")

def assignMultiArm(toCover: Set[(Treatment, Treatment)], studies: Set[Study]) =
	Map[Study, Treatment]()

def assignBaselines(network: Network, st: Tree[Treatment])
: Map[Study, Treatment] = {
	val toCover = network.inconsistencies(best).flatMap(a => a.edgeSet)
	val twoArm = network.studies.filter(study => study.treatments.size == 2)
	val multiArm = network.studies -- twoArm
	val covered = twoArm.flatMap(study => study.treatmentGraph.edgeSet)

	val twoArmMap = Map[Study, Treatment]() ++ twoArm.map(study => (study, study.treatments.toList.sort((a, b) => a < b).head))

	val leftToCover = toCover -- covered
	twoArmMap ++ assignMultiArm(leftToCover, multiArm)
}

println(assignBaselines(network, best))
