class Study(_id: String, treatments: Set[Treatment]) {
	val id = _id
	override def toString = "Study(" + _id + ") = " + treatments

	def treatmentGraph: Graph[Treatment] = {
		val tList = treatments.toList.sort((a, b) => (a < b))
		var edgeSet = Set[(Treatment, Treatment)]()
		for (i <- 0 to tList.size - 2) {
			for (j <- (i + 1) to tList.size - 1) {
				val edge: (Treatment, Treatment) = (tList(i), tList(j))
				edgeSet = edgeSet + edge
			}
		}
		new Graph[Treatment](edgeSet)
	}
}

object Study {
	def fromXML(node: scala.xml.Node,
			treatments: Map[String, Treatment]): Study =
		new Study((node \ "@id").text, treatmentsFromXML(node \ "measurement",
			treatments))

	private def treatmentsFromXML(nodes: scala.xml.NodeSeq,
			treatments: Map[String, Treatment]): Set[Treatment] = 
		Set[Treatment]() ++ 
		{for {node <- nodes} yield getTreatment(treatments, (node \ "@treatment").text)}

	private def getTreatment(treatments: Map[String, Treatment], id: String): Treatment = 
		treatments.get(id) match {
			case Some(treatment) => treatment
			case None => throw new IllegalStateException("Non-existent treatment ID referred to!")
		}
}

final class Treatment(_id: String, _desc: String) {
	val id: String = _id
	val description = _desc
	override def toString = "Treatment(" + id + ")"

	def this(_id: String) = this(_id, "")

	def <(that: Treatment): Boolean = id < that.id

	override def equals(other: Any): Boolean =
		other match {
			case that: Treatment =>
				id == that.id
			case _ => false
		}
}

object Treatment {
	def fromXML(node: scala.xml.Node): Treatment =
		new Treatment((node \ "@id").text, node.text)
}

class Network(_treatments: Set[Treatment], _studies: Set[Study]) {
	val treatments = _treatments
	val studies = _studies
	override def toString = treatments.toString + studies.toString

	val treatmentGraph: Graph[Treatment] = {
		var graph = new Graph[Treatment](Set[(Treatment, Treatment)]())
		for (s <- studies) {
			graph = graph.union(s.treatmentGraph)
		}
		graph
	}

	def supportingEvidence(cycle: Graph[Treatment])
		: Set[Graph[Treatment]] = {
		Set[Graph[Treatment]]() ++ {
			for {s <- studies
				val edges = s.treatmentGraph.intersection(cycle).edgeSet
				if !edges.isEmpty
			} yield new Graph[Treatment](edges)
		}
	}

	val edgeVector: List[(Treatment, Treatment)] =
		treatmentGraph.edgeVector((a, b) => a < b)
}

object Network {
	def fromXML(node: scala.xml.Node): Network =  {
		val treatments = treatmentsFromXML((node \ "treatments")(0))
		new Network(Set[Treatment]() ++ treatments.values, 
			studiesFromXML((node \ "studies")(0), treatments))
	}

	def treatmentsFromXML(n: scala.xml.Node): Map[String, Treatment] =
		Map[String, Treatment]() ++
		{for (node <- n \ "treatment") yield ((node \ "@id").text, Treatment.fromXML(node))}

	def studiesFromXML(n: scala.xml.Node, treatments: Map[String, Treatment]): Set[Study] =
		Set[Study]() ++
		{for (node <- n \ "study") yield Study.fromXML(node, treatments)}
}

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

class Graph[T](edges: Set[(T, T)]) {
	val edgeSet: Set[(T, T)] = edges
	val vertexSet: Set[T] = vertices(edges)

	def union(other: Graph[T]) = new Graph[T](edgeSet ++ other.edgeSet)
	def intersection(other: Graph[T]) = new Graph[T](edgeSet ** other.edgeSet)

	def edgeVector(f: (T, T) => Boolean): List[(T, T)] =
		edgeSet.toList.sort((a, b) =>
			if (a._1 == b._1) f(a._2, b._2)
			else f(a._1, b._1))

	def incidenceVector(edgeVector: List[(T, T)]): List[Boolean] = {
		edgeVector match {
			case e :: l1 => edgeSet.contains(e) :: incidenceVector(l1)
			case List() => List[Boolean]()
		}
	}

	private def vertices(edges: Set[(T, T)]): Set[T] = {
		edges.flatMap(e => List(e._1, e._2))
	}
}

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
println(network.supportingEvidence(cycle))

println("-- Edge Vector for network: ")
println(network.edgeVector)

println("-- Incidence Vector of studies: ")
for (s <- network.studies) {
	println(s.treatmentGraph.incidenceVector(network.edgeVector))
}

println("-- Incidence Vector of evidence supporting ABC: ")
for (s <- network.supportingEvidence(cycle)) {
	println(s.incidenceVector(network.edgeVector))
}
