
/*
val study = Study.fromXML(<study id="1"><measurement treatment="A"/></study>,
	Map[String, Treatment](("A", new Treatment("A", "Viagra"))))
println(study)

println(new Treatment("A", "Blaat") == new Treatment("A", "Bleh"))
*/

val networkXML =
	<network>
		<treatments>
			<treatment id="A"/>
			<treatment id="B"/>
			<treatment id="C"/>
			<treatment id="D"/>
		</treatments>
		<studies>
			<study id="1">
				<measurement treatment="A"/>
				<measurement treatment="B"/>
			</study>
			<study id="2">
				<measurement treatment="A"/>
				<measurement treatment="B"/>
				<measurement treatment="C"/>
			</study>
			<study id="1">
				<measurement treatment="D"/>
				<measurement treatment="B"/>
			</study>
		</studies>
	</network>
val network = Network.fromXML(networkXML)
println(network)


val graph = new Graph[String](Set[(String, String)](("A", "B"), ("B", "C")))
val graphB = new Graph[String](Set[(String, String)](("A", "C")))
val graphC = new Graph[String](Set[(String, String)](("A", "B")))
println(graph.vertexSet)
println(graph.edgeSet)

println(graph.union(graphB).edgeSet)
println(graph.intersection(graphC).edgeSet)

println("-- Treatment networks of studies:")
for (s <- network.studies) {
	println(s.treatmentGraph.edgeSet)
}
println("-- Entire network: ")
println(network.treatmentGraph.edgeSet)

println("-- Evidence supporting ABC: ")
val cycle = new Graph[Treatment](Set[(Treatment, Treatment)]((new Treatment("A"), new Treatment("B")), (new Treatment("A"), new Treatment("C")), (new Treatment("B"), new Treatment("C"))))
network.supportingEvidence(cycle).foreach(x => println(x.edgeSet))

println("-- Edge Vector for network: ")
println(network.edgeVector)

println("-- Incidence Vector of studies: ")
for (s <- network.studies) {
	println(s.treatmentGraph.incidenceVector(network.edgeVector))
}

println("-- Incidence Vector of evidence supporting ABC: ")
for (s <- network.supportingEvidence(cycle)) {
	println(s.incidenceVector(cycle.edgeVector))
}
