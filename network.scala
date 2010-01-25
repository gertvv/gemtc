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

val studyMap = indexMap(network.studies.toList.sort((a, b) => a.id < b.id))
val treatmentMap = indexMap(network.treatments.toList.sort((a, b) => a < b))

//val dataIt: Iterator[(Study, Measurement)] = (
//for {study <- network.studies.toList; measurement <- study.measurements.values} yield (study, measurement))
//
//val data = dataIt.toList
//
//val s = data.map(a => studyMap(a._1))

val data = network.studies.toList.sort((a, b) => a.id < b.id).flatMap(
	study => study.measurements.map(m => (study, m._2)))

val s = data.map(a => studyMap(a._1))

println("s <- c(" + s.mkString(", ") + ")")
println("t <- c(" + data.map(a => treatmentMap(a._2.treatment)).mkString(", ") + ")")
println("r <- c(" + data.map(a => a._2.responders).mkString(", ") + ")")
println("n <- c(" + data.map(a => a._2.sampleSize).mkString(", ") + ")")
